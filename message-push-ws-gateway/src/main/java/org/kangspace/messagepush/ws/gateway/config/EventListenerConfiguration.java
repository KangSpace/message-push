package org.kangspace.messagepush.ws.gateway.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.ws.gateway.nacos.NacosDynamicServerListListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事件监听配置
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@Configuration
public class EventListenerConfiguration {

    /**
     * 服务上下线监听
     *
     * @param properties
     * @return NacosDynamicServerListListener
     */
    @Bean
    public NacosDynamicServerListListener nacosDynamicServerListListener(NacosDiscoveryProperties properties) {
        return new NacosDynamicServerListListener(properties, MessagePushConstants.MESSAGE_WS_SERVICE_ID);
    }
}