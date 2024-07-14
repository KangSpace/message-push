package org.kangspace.messagepush.ws.core.debug;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.ws.core.websocket.session.WebSocketUserSessionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

/**
 * websocket session连接数打印
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/18
 */
@Slf4j
@Service
public class DebugWebsocketSessionCountScheduler {
    /**
     * WebSocketUserSessionManager
     */
    private final WebSocketUserSessionManager webSocketUserSessionManager;

    private ThreadPoolTaskScheduler scheduler;

    public DebugWebsocketSessionCountScheduler(WebSocketUserSessionManager webSocketUserSessionManager) {
        this.webSocketUserSessionManager = webSocketUserSessionManager;
        if (!log.isDebugEnabled()) {
            return;
        }
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.setPoolSize(1);
        this.scheduler.initialize();
        startPrint();
    }

    /**
     * 开始日志打印任务
     */
    private void startPrint() {
        log.debug("WebsocketSession数打印: 定时任务开始!");
        this.scheduler.scheduleAtFixedRate(() -> print(), 1000L);

    }

    /**
     * 打印日志
     */
    private void print() {
        // 应用数
        int appConnectCnt = webSocketUserSessionManager.getAppUserSessionsMap().size();
        // 用户数
        int userConnectCnt = webSocketUserSessionManager.getAppUserSessionsMap().values()
                .stream().mapToInt(t -> t.size()).sum();
        // 总连接数
        long sessionConnectCnt = webSocketUserSessionManager.getAppUserSessionsMap().values()
                .stream().flatMap(t -> t.values().stream()).mapToLong(t -> t.size()).sum();
        log.debug("当前Session连接数汇总: 应用数:{}, 用户数:{} ,总连接数:{}", appConnectCnt, userConnectCnt, sessionConnectCnt);
    }
}
