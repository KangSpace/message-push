package org.kangspace.messagepush.ws.core.websocket.session;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * WebSocket用户Session管理器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@Getter
public class WebSocketUserSessionManager implements WebSocketSessionManager {
    /**
     * 同步操作锁
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * 用户和Session关系
     *
     * @see CopyOnWriteArrayList
     */
    private ConcurrentHashMap<String, List<SessionHolder>> userSessionsMap;
    /**
     * AppKey下用户和Session关系
     *
     * @see CopyOnWriteArrayList
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, List<SessionHolder>>> appUserSessionsMap;

    private ConcurrentHashSet<WebSocketSession> managedSessions;


    public WebSocketUserSessionManager() {
        this.userSessionsMap = new ConcurrentHashMap<>(16);
        this.appUserSessionsMap = new ConcurrentHashMap<>(16);
        this.managedSessions = new ConcurrentHashSet<>(16);
    }


    @Override
    public SessionHolder addSession(String key, SessionHolder sessionHolder) {
        lock.lock();
        try {
            List<SessionHolder> sessionHolders = this.userSessionsMap.getOrDefault(key, new CopyOnWriteArrayList<>());
            boolean isNewKey = CollectionUtils.isEmpty(sessionHolders);
            sessionHolders.add(sessionHolder);
            if (isNewKey) {
                this.userSessionsMap.put(key, sessionHolders);
            }
            managedSessions.add(sessionHolder.getSession());
        } finally {
            lock.unlock();
        }
        return sessionHolder;
    }

    @Override
    public boolean removeSession(String key, WebSocketSession session) {
        lock.lock();
        try {
            List<SessionHolder> sessionHolders = this.userSessionsMap.getOrDefault(key, new CopyOnWriteArrayList<>());
            boolean del = removeUserSessionMapSession(sessionHolders, session);
            if (!CollectionUtils.isEmpty(this.userSessionsMap)) {
                this.userSessionsMap.forEach((k, v) -> {
                    if (CollectionUtils.isEmpty(v)) {
                        this.userSessionsMap.remove(k);
                    }
                });
            }
            if (!CollectionUtils.isEmpty(this.appUserSessionsMap)) {
                this.appUserSessionsMap.values().stream().filter(k -> !CollectionUtils.isEmpty(k.values()))
                        .forEach(userSessionMaps -> {
                            userSessionMaps.values().forEach(userSessionMap -> removeUserSessionMapSession(userSessionMap, session));
                            userSessionMaps.forEach((k, v) -> {
                                if (CollectionUtils.isEmpty(v)) {
                                    userSessionMaps.remove(k);
                                }
                            });
                        });
                this.appUserSessionsMap.forEach((k, v) -> {
                    if (CollectionUtils.isEmpty(v)) {
                        this.appUserSessionsMap.remove(k);
                    }
                });
            }
            managedSessions.remove(session);
            return del;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除用户sessionMap中的Session
     *
     * @param sessionHolders
     * @param session
     */
    private boolean removeUserSessionMapSession(Collection<SessionHolder> sessionHolders, WebSocketSession session) {
        lock.lock();
        try {
            boolean del = false;
            if (!CollectionUtils.isEmpty(sessionHolders)) {
                for (Iterator<SessionHolder> iterator = sessionHolders.iterator(); iterator.hasNext(); ) {
                    SessionHolder sessionHolder = iterator.next();
                    if (sessionHolder.getSession().equals(session)) {
                        iterator.remove();
                        del = true;
                    }
                }
            }
            return del;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SessionHolder addSession(String appKey, String key, SessionHolder sessionHolder) {
        lock.lock();
        try {
            ConcurrentHashMap<String, List<SessionHolder>> userSessionsMap = this.appUserSessionsMap.getOrDefault(appKey, new ConcurrentHashMap<>(16));
            // 是否新APP加入
            boolean isNewApp = CollectionUtils.isEmpty(userSessionsMap);
            List<SessionHolder> sessionHolders = userSessionsMap.getOrDefault(key, new ArrayList<>());
            // 是否新用户Session加入
            boolean isNewUserSessions = CollectionUtils.isEmpty(sessionHolders);
            sessionHolders.add(sessionHolder);
            if (isNewUserSessions) {
                userSessionsMap.put(key, sessionHolders);
            }
            if (isNewApp) {
                this.appUserSessionsMap.put(appKey, userSessionsMap);
            }
            managedSessions.add(sessionHolder.getSession());
        } finally {
            lock.unlock();
        }
        return sessionHolder;
    }

    @Override
    public boolean sessionCheck(WebSocketSession session) {
        return managedSessions.contains(session);
    }

    @Override
    public Map<String, List<SessionHolder>> getKeySessions(String appKey) {
        ConcurrentHashMap<String, List<SessionHolder>> concurrentHashMap = appUserSessionsMap.get(appKey);
        return CollectionUtils.isEmpty(concurrentHashMap) ? Collections.emptyMap() : concurrentHashMap;
    }
}
