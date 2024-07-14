package org.kangspace.messagepush.ws.gateway.hash;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 打印一致性Hash相关数据定时器(Debug级别打印)
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/4
 */
@Slf4j
public class DebugPrintHashingScheduler {
    /**
     * 一致性Hash处理Bean
     */
    private final ConsistencyHashing consistencyHashing;

    private ThreadPoolTaskScheduler scheduler;

    public DebugPrintHashingScheduler(ConsistencyHashing consistencyHashing) {
        this.consistencyHashing = consistencyHashing;
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
        log.debug("一致性Hash数据加载: 定时任务开始!");
        this.scheduler.scheduleAtFixedRate(() -> print(), 3000L);

    }

    /**
     * 打印日志
     */
    private void print() {
        log.info("当前物理节点数:[{}] ,当前numberOfVNode:[{}]", this.consistencyHashing.getPhysicalNodes(),
                this.consistencyHashing.getNumberOfVirtualNode());
    }

}
