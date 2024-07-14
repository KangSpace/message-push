package org.kangspace.messagepush.core.hash;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.hash.algoithm.HashAlgorithm;
import org.kangspace.messagepush.core.hash.algoithm.KetamaHashAlgorithm;
import org.kangspace.messagepush.core.util.MD5Util;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 一致性hash实现
 * 基于Ketama 一致性Hash算法:
 * 1. 取node的md5
 * 2. 再将md5值每4字节计算一个Hash Key存到Hash环中,即每个node会有4个hash节点
 * 3. 为node的所有虚拟节点做2的处理,并将结果存到Hash环中
 * 4. 每个物理节点的虚拟节点在hash环上最好分配100-200个点来抑制分布不均匀，最大限度地减小服务器增减时的缓存重新分布
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/22
 */
@Data
@Slf4j
public class ConsistencyHashing<T> {
    /**
     * 虚拟节点分割符
     */
    private static final String VIRTUAL_DELIMITER = "#VN";
    /**
     * hash 分组大小
     */
    private static final int HASH_GROUP_SIZE = 4;
    /**
     * 并发锁
     */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();
    /**
     * <pre>
     * 每个物理节点的虚拟节点数
     * 每个物理节点的虚拟节点在hash环上最好分配100-200个点来抑制分布不均匀，最大限度地减小服务器增减时的缓存重新分布
     * 计算虚拟节点总数时需考虑:numberOfVirtualNode中的每个节点会创建4个虚拟节点
     * </pre>
     */
    private Integer numberOfVirtualNode;
    /**
     * Hash算法
     */
    private HashAlgorithm hashAlgorithm = new KetamaHashAlgorithm();
    /**
     * 物理节点的摘要
     *
     * @see MD5Util#hashDigest(Collection)
     */
    private String physicalNodesDigest;
    /**
     * 物理节点列表
     */
    private List<PhysicalNode<T>> physicalNodes = new ArrayList<>();
    /**
     * Hash环
     * key: hash值
     * value: 虚拟节点
     */
    private TreeMap<Long, VirtualNode<T>> ring = new TreeMap<>();

    public ConsistencyHashing(int numberOfVirtualNode, List<String> physicalNodes) {
        this.numberOfVirtualNode = numberOfVirtualNode;
        if (!CollectionUtils.isEmpty(physicalNodes)) {
            List<PhysicalNode<T>> pNodes = physicalNodes.stream().distinct()
                    .map(node -> new PhysicalNode<T>(hashAlgorithm.hashing(node), node))
                    .collect(Collectors.toList());
            pNodes.forEach(node -> addVirtualNode(node, numberOfVirtualNode));
            setPhysicalNodes(pNodes);
        }
    }

    public ConsistencyHashing(List<PhysicalNode<T>> physicalNodes, int numberOfVirtualNode) {
        this.numberOfVirtualNode = numberOfVirtualNode;
        if (!CollectionUtils.isEmpty(physicalNodes)) {
            physicalNodes.forEach(node -> addVirtualNode(node, numberOfVirtualNode));
            setPhysicalNodes(physicalNodes);
        }
    }

    public ConsistencyHashing(Map<String, VirtualNode<T>> fromRing, int numberOfVirtualNode) {
        if (!CollectionUtils.isEmpty(fromRing)) {
            fromRing.forEach((k, v) -> this.ring.put(Long.valueOf(k), v));
            List<PhysicalNode<T>> physicalNodes = fromRing.values().stream().map(t -> t.getPhysicalNode()).collect(Collectors.toList());
            List<PhysicalNode<T>> distinctPhysicalNodes = new ArrayList<>(physicalNodes.stream().collect(Collectors.toMap(k -> k.getNode(), v -> v, (v1, v2) -> v1)).values());
            int totalVirtualCount = fromRing.size();
            long physicalNodeCount = distinctPhysicalNodes.size();
            this.numberOfVirtualNode = Math.toIntExact(physicalNodeCount > 0 ? totalVirtualCount / 4 / physicalNodeCount : numberOfVirtualNode);
            setPhysicalNodes(distinctPhysicalNodes);
        }
    }

    private void setPhysicalNodes(List<PhysicalNode<T>> physicalNodes) {
        this.physicalNodes = physicalNodes;
        List<String> nodeIpPorts = physicalNodes.stream().map(t -> t.getNode()).distinct().collect(Collectors.toList());
        this.physicalNodesDigest = MD5Util.hashDigest(nodeIpPorts);
    }

    private void addPhysicalNodes(PhysicalNode<T> physicalNode) {
        this.physicalNodes.add(physicalNode);
        List<String> nodeIpPorts = this.physicalNodes.stream().map(t -> t.getNode()).collect(Collectors.toList());
        this.physicalNodesDigest = MD5Util.hashDigest(nodeIpPorts);
    }

    private void removePhysicalNodes(PhysicalNode<T> physicalNode) {
        this.physicalNodes = this.physicalNodes.stream().filter(t -> !t.getNode().equals(physicalNode.getNode())).collect(Collectors.toList());
        List<String> nodeIpPorts = this.physicalNodes.stream().map(t -> t.getNode()).collect(Collectors.toList());
        this.physicalNodesDigest = MD5Util.hashDigest(nodeIpPorts);
    }

    /**
     * 获取节点hash
     *
     * @param node node
     * @return hash结果
     */
    public Long getNodeHash(String node) {
        return this.hashAlgorithm.hashing(node);
    }


    /**
     * 获取data所在环的虚拟节点
     *
     * @param data 数据节点
     * @return virtualNode
     */
    public VirtualNode<T> getVirtualNode(String data) {
        if (ring.isEmpty()) {
            return null;
        }
        Long hash = hashAlgorithm.hashing(data);
        r.lock();
        try {
            if (!ring.containsKey(hash)) {
                SortedMap<Long, VirtualNode<T>> tailMap = ring.tailMap(hash);
                hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
            }
            return ring.get(hash);
        } finally {
            r.unlock();
        }
    }

    /**
     * 添加虚拟节点(使用Ketama一致性HASH算法)
     *
     * @param physicalNode        物理节点
     * @param numberOfVirtualNode 每个物理节点的虚拟节点数
     */
    private void addVirtualNode(Node<T> physicalNode, int numberOfVirtualNode) {
        w.lock();
        try {
            // / HASH_GROUP_SIZE
            for (int i = 0; i < numberOfVirtualNode; i++) {
                String virtualNode = getVirtualNode(physicalNode, i);
                byte[] digest = hashAlgorithm.md5(virtualNode);
                for (int j = 0; j < HASH_GROUP_SIZE; j++) {
                    Long hash = hashAlgorithm.hashing(digest, j);
                    ring.put(hash, new VirtualNode(hash, virtualNode, (PhysicalNode) physicalNode));
                }
            }
        } finally {
            w.unlock();
        }
    }


    /**
     * 删除一个物理节点
     *
     * @param physicalNode 物理节点
     */
    public void removeNode(PhysicalNode<T> physicalNode) {
        if (ring.isEmpty()) {
            return;
        }
        w.lock();
        try {
            // 实现注意遍历删除可能存在的并发修改异常
            Iterator<Long> iterator = ring.keySet().iterator();
            while (iterator.hasNext()) {
                Long nodeHashKey = iterator.next();
                VirtualNode<T> virtualNode = ring.get(nodeHashKey);
                if (virtualNode.isVirtualOf(physicalNode)) {
                    iterator.remove();
                }
            }
            removePhysicalNodes(physicalNode);
        } finally {
            w.unlock();
        }
    }

    /**
     * 添加一个物理节点
     *
     * @param physicalNode 物理节点
     */
    public void addNode(PhysicalNode<T> physicalNode) {
        w.lock();
        try {
            addPhysicalNodes(physicalNode);
            addVirtualNode(physicalNode, this.numberOfVirtualNode);
        } finally {
            w.unlock();
        }
    }

    /**
     * 获取虚拟节点节点node值
     *
     * @param physicalNode 物理节点
     * @param number       下表
     * @return 新的node字符串
     */
    private String getVirtualNode(Node<T> physicalNode, int number) {
        return physicalNode.getNode() + VIRTUAL_DELIMITER + number;
    }

    /**
     * 获取虚拟节点数量
     *
     * @return 虚拟节点数量
     */
    public int getVirtualNodeCount() {
        return ring.size();
    }
}
