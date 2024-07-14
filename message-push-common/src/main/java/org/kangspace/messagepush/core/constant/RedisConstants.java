package org.kangspace.messagepush.core.constant;

/**
 * Redis相关常量类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
public interface RedisConstants {

    String DELIMTRER = ":";
    /**
     * 消息推送相关key前缀
     */
    String MESSAGE_PUSH_BASE_KEY = "message_push";
    /**
     * 消息推送相关Hash环key
     */
    String MESSAGE_PUSH_HASH_RING_KEY = MESSAGE_PUSH_BASE_KEY + DELIMTRER + "hash_ring";
    /**
     * 消息推送相关Hash环更新同步锁key
     */
    String MESSAGE_PUSH_HASH_RING_STORE_LOCK_KEY = MESSAGE_PUSH_BASE_KEY + DELIMTRER + "hash_ring_store_lock";
    /**
     * 消息推送相关Hash环更新同步锁超时时间,单位s
     */
    long MESSAGE_PUSH_HASH_RING_STORE_LOCK_EXPIRE_SEC = 3;

    /**
     * 实例用户映射 map key
     * key: 服务实例 ip:port
     * value: array 用户ID列表
     */
    String MESSAGE_PUSH_INSTANCE_USER_MAP_KEY = MESSAGE_PUSH_BASE_KEY + DELIMTRER + "instance_user";


}
