package org.kangspace.messagepush.consumer.core.config;

import org.kangspace.messagepush.consumer.core.hash.HashRouterLoader;
import org.kangspace.messagepush.consumer.core.redis.RedisService;
import org.kangspace.messagepush.core.constant.RedisConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消费者公共配置加载类
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/3
 */
@Configuration
public class PushConsumerConfiguration {

    /**
     * HashRouter加载
     *
     * @return HashRouterLoader
     */
    @Bean
    public HashRouterLoader hashRouterLoader(RedisService redisService) {
        return new HashRouterLoader(redisService, RedisConstants.MESSAGE_PUSH_HASH_RING_KEY);
    }

}
