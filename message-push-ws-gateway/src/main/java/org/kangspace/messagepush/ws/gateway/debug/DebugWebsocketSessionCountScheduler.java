package org.kangspace.messagepush.ws.gateway.debug;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.ws.gateway.filter.session.WebSocketUserSessionManager;
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
     * 一致性Hash处理Bean
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
        // 用户数
        int userConnectCnt = webSocketUserSessionManager.getUserSessionsMap().size();
        // 总连接数
        long sessionConnectCnt = webSocketUserSessionManager.getUserSessionsMap().values()
                .stream().filter(t -> t != null).flatMap(t -> t.stream()).count();
        log.info("当前Session连接数汇总: 用户数:{} ,总连接数:{}", userConnectCnt, sessionConnectCnt);
    }
}
