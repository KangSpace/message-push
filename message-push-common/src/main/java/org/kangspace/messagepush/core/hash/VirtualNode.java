package org.kangspace.messagepush.core.hash;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 物理服务节点
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/22
 */
@Data
@NoArgsConstructor
public class VirtualNode<T> implements Node<T> {

    /**
     * 物理节点
     */
    private PhysicalNode<T> physicalNode;
    /**
     * 节点数据
     */
    private String node;
    /**
     * 节点key(HASH)
     */
    private Long key;

    /**
     * 节点数据
     */
    private T data;

    public VirtualNode(Long key, String node, PhysicalNode<T> physicalNode) {
        this(key, node, physicalNode, null);
    }

    public VirtualNode(Long key, String node, PhysicalNode<T> physicalNode, T data) {
        Objects.requireNonNull(node, "PhysicalNode [node] must be not null!");
        this.key = key;
        this.node = node;
        this.physicalNode = physicalNode;
        this.data = data;
    }

    @Override
    public boolean isPhysicalNode() {
        return false;
    }

    /**
     * 检查是否指定物理节点的虚拟节点
     *
     * @param physicalNode 物理节点
     * @return boolean
     */
    public boolean isVirtualOf(PhysicalNode<T> physicalNode) {
        return Objects.equals(physicalNode.getNode(), this.getPhysicalNode().getNode());
    }
}
