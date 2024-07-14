package org.kangspace.messagepush.consumer.core.hash;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.consumer.core.redis.RedisService;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.kangspace.messagepush.core.hash.VirtualNode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;

/**
 * 一致性Hash数据加载类
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/3
 */
@Slf4j
@Data
public class HashRouterLoader {
    private final RedisService redisService;
    /**
     * 一致性Hash缓存Key
     */
    private final String redisKey;
    /**
     * 单线程定时线程池
     */
    private ThreadPoolTaskScheduler scheduler;
    /**
     * 一致性Hash数据
     */
    private ConsistencyHashing hashRouter;

    public HashRouterLoader(RedisService redisService, String redisKey) {
        this.redisService = redisService;
        this.redisKey = redisKey;
        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.setPoolSize(1);
        this.scheduler.initialize();
        start();
    }

    private void setHashRouter(ConsistencyHashing hashRouter) {
        this.hashRouter = hashRouter;
    }

    /**
     * 加载一致性Hash数据
     */
    private void load() {
        final Map<String, VirtualNode> ring = redisService.hGetAll(this.redisKey, VirtualNode.class);
        ConsistencyHashing hashRouter = new ConsistencyHashing(ring, MessagePushConstants.NUMBER_OF_VIRTUAL_NODE);
        this.setHashRouter(hashRouter);
        if (log.isDebugEnabled()) {
            log.debug("一致性Hash数据加载: 加载成功, 物理节点:[{}]", hashRouter.getPhysicalNodes());
        }
    }

    /**
     * 开始定时job
     */
    private void start() {
        log.debug("一致性Hash数据加载: 定时任务开始!");
        this.scheduler.scheduleAtFixedRate(() -> load(), 3000L);
    }

    /**
     * 获取物理节点node值
     *
     * @param key 需要hash的key
     * @return 目标node值
     */
    public String getPhysicalNode(String key) {
        return this.getHashRouter().getVirtualNode(key).getPhysicalNode().getNode();
    }
}
