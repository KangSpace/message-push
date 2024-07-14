package org.kangspace.messagepush.core.websocket;

import java.util.List;
import java.util.Map;

/**
 * WebSocket用户Session管理器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
public interface WebSocketSessionManager<T> {
    /**
     * 添加Session
     *
     * @param key           session关联的推送对象
     * @param sessionHolder sessionHolder
     * @return SessionHolder
     */
    T addSession(String key, T sessionHolder);

    /**
     * 删除Session
     *
     * @param key           session关联的推送对象
     * @param socketSession WebSocketSession
     * @return boolean
     */
    boolean removeSession(String key, T socketSession);

    /**
     * 为应用添加用户Session关系
     *
     * @param appKey        应用ID
     * @param key           session关联的推送对象
     * @param sessionHolder sessionHolder
     * @return SessionHolder
     */
    T addSession(String appKey, String key, T sessionHolder);

    /**
     * 检查Session是否已添加
     *
     * @param session WebSocketSession
     * @return boolean
     */
    boolean sessionCheck(T session);

    /**
     * 通过appKey获取所有用户Session信息
     *
     * @param appKey appKey
     * @return 用户SessionMap
     */
    Map<String, List<T>> getKeySessions(String appKey);
}
