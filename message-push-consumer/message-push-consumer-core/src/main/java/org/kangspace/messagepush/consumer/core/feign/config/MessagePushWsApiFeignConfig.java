package org.kangspace.messagepush.consumer.core.feign.config;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 消息Websocket推送接口Feign配置
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/6
 */
@Configuration
@Import(FeignClientsConfiguration.class)
public class MessagePushWsApiFeignConfig {
    /**
     * 默认服务节点请求头Key
     */
    public static final String DEFAULT_SERVICE_NODE_HEADER_KEY = "serviceNode";
    /**
     * 默认访问协议
     */
    private final String DEFAULT_HTTP_SCHEMA_PROTOCOL = "http://";

    /**
     * 动态URL路径处理
     *
     * @return
     */
    @Bean
    public RequestInterceptor dynamicUrlInterceptor() {
        return template -> {
            String serviceNode = template.request().headers().get(DEFAULT_SERVICE_NODE_HEADER_KEY).stream().findFirst().orElse(null);
            if (serviceNode != null) {
                template.target(DEFAULT_HTTP_SCHEMA_PROTOCOL + serviceNode);
            }
        };
    }

}
