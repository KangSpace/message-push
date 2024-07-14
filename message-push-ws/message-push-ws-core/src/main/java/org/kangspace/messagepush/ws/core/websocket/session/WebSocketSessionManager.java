package org.kangspace.messagepush.ws.core.websocket.session;

import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * WebSocket用户Session管理器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
public interface WebSocketSessionManager {
    /**
     * 添加Session
     *
     * @param key           session关联的推送对象
     * @param sessionHolder sessionHolder
     * @return SessionHolder
     */
    SessionHolder addSession(String key, SessionHolder sessionHolder);

    /**
     * 删除Session
     *
     * @param key           session关联的推送对象
     * @param socketSession WebSocketSession
     * @return boolean
     */
    boolean removeSession(String key, WebSocketSession socketSession);

    /**
     * 为应用添加用户Session关系
     *
     * @param appKey        应用ID
     * @param key           session关联的推送对象
     * @param sessionHolder sessionHolder
     * @return SessionHolder
     */
    SessionHolder addSession(String appKey, String key, SessionHolder sessionHolder);

    /**
     * 检查Session是否已添加
     *
     * @param session WebSocketSession
     * @return boolean
     */
    boolean sessionCheck(WebSocketSession session);

    /**
     * 通过appKey获取所有用户Session信息
     *
     * @param appKey appKey
     * @return 用户SessionMap
     */
    Map<String, List<SessionHolder>> getKeySessions(String appKey);
}
