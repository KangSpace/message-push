package org.kangspace.messagepush.ws.gateway.filter.session;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.event.NacosServiceUpInfo;
import org.kangspace.messagepush.core.event.NacosServiceUpdateEvent;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.kangspace.messagepush.core.hash.VirtualNode;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * WebSocket用户Session管理器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@Slf4j
@Getter
public class WebSocketUserSessionManager implements SmartApplicationListener {
    /**
     * 同步操作锁
     */
    private ReentrantLock lock = new ReentrantLock();

    /**
     * 用户和Session关系
     */
    private ConcurrentHashMap<String, List<SessionProxyHolder>> userSessionsMap;


    public WebSocketUserSessionManager() {
        this.userSessionsMap = new ConcurrentHashMap<>(16);
    }


    /**
     * 添加Session信息
     *
     * @param uid
     * @param sessionHolder {@link SessionProxyHolder}
     * @return {@link SessionProxyHolder}
     */
    public SessionProxyHolder addSession(String uid, SessionProxyHolder sessionHolder) {
        lock.lock();
        try {
            List<SessionProxyHolder> sessionHolders = this.userSessionsMap.getOrDefault(uid, new ArrayList<>());
            boolean isNewKey = CollectionUtils.isEmpty(sessionHolders);
            sessionHolders.add(sessionHolder);
            if (isNewKey) {
                this.userSessionsMap.put(uid, sessionHolders);
            }
        } finally {
            lock.unlock();
        }
        return sessionHolder;
    }

    /**
     * 删除Session
     *
     * @param uid
     * @param session
     * @return
     */
    public boolean removeSession(String uid, WebSocketSession session) {
        lock.lock();
        try {
            List<SessionProxyHolder> sessionHolders = this.userSessionsMap.getOrDefault(uid, new ArrayList<>());
            boolean del = removeUserSessionMapSession(sessionHolders, session);
            this.userSessionsMap.forEach((k, v) -> {
                if (CollectionUtils.isEmpty(v)) {
                    this.userSessionsMap.remove(k);
                }
            });
            return del;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除用户sessionMap中的Session
     *
     * @param sessionHolders {@link SessionProxyHolder} 列表
     * @param session        当前网关与客户端建立的session
     * @return boolean
     */
    private boolean removeUserSessionMapSession(Collection<SessionProxyHolder> sessionHolders, WebSocketSession session) {
        boolean del = false;
        if (!CollectionUtils.isEmpty(sessionHolders)) {
            for (Iterator<SessionProxyHolder> iterator = sessionHolders.iterator(); iterator.hasNext(); ) {
                SessionProxyHolder sessionHolder = iterator.next();
                if (sessionHolder.getSession().equals(session)) {
                    iterator.remove();
                    del = true;
                }
            }
        }
        return del;
    }

    /**
     * 用户Session下线处理
     *
     * @param consistencyHashing {@link ConsistencyHashing}
     */
    public void offlineUserSessionHandle(ConsistencyHashing consistencyHashing) {
        lock.lock();
        try {
            if (!CollectionUtils.isEmpty(this.userSessionsMap) && consistencyHashing != null) {
                List<String> removeKeys = new ArrayList<>();
                AtomicInteger closedCount = new AtomicInteger(0);
                userSessionsMap.forEach((uid, sessionHolders) -> {
                    if (!CollectionUtils.isEmpty(sessionHolders)) {
                        VirtualNode vn = consistencyHashing.getVirtualNode(uid);
                        if (vn != null) {
                            String newNode = vn.getPhysicalNode().getNode();
                            String oldNode = sessionHolders.get(0).getServiceNode();
                            if (!newNode.equals(oldNode)) {
                                sessionHolders.forEach(sessionProxyHolder -> {
                                    sessionProxyHolder.getSession().close(new CloseStatus(CloseStatus.SERVICE_RESTARTED.getCode(), "新服务上线,服务重平衡!")).toProcessor();
                                    sessionProxyHolder.getProxySession().close(new CloseStatus(CloseStatus.SERVICE_RESTARTED.getCode(), "新服务上线,服务重平衡!")).toProcessor();
                                    closedCount.getAndAdd(1);
                                });
                                removeKeys.add(uid);
                            }
                        }
                    }
                });
                removeKeys.forEach(userSessionsMap::remove);
                log.info("新服务上线,服务重平衡处理: 结束, 剔除连接数:[{}]", closedCount.get());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event != null && event.getSource() != null) {
            ConsistencyHashing consistencyHashing = ((NacosServiceUpInfo) event.getSource()).getConsistencyHashing();
            offlineUserSessionHandle(consistencyHashing);
        }
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
        return NacosServiceUpdateEvent.class.isAssignableFrom(aClass);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return NacosServiceUpInfo.class.isAssignableFrom(sourceType);
    }
}
