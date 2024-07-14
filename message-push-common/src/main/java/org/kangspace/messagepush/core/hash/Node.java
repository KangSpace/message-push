package org.kangspace.messagepush.core.hash;

/**
 * 基本的服务节点(hash环上的节点)
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/22
 */
public interface Node<T> {
    /**
     * 获取节点Key(一般为HASH值)
     *
     * @return key
     */
    Long getKey();

    /**
     * 获取node原始值
     *
     * @return String
     */
    String getNode();

    /**
     * 是否物理节点
     *
     * @return boolean
     */
    boolean isPhysicalNode();

    /**
     * 获取节点数据
     *
     * @return T
     */
    T getData();

}
