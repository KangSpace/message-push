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
public class PhysicalNode<T> implements Node<T> {
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

    public PhysicalNode(Long key, String node) {
        this(key, node, null);
    }

    public PhysicalNode(Long key, String node, T data) {
        Objects.requireNonNull(node, "PhysicalServiceNode [node] must be not null!");
        this.key = key;
        this.node = node;
        this.data = data;
    }

    @Override
    public boolean isPhysicalNode() {
        return true;
    }
}
