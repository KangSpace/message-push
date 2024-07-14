package org.kangspace.messagepush.ws.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 网关相关配置
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "message-push.gateway")
public class MessagePushWsGatewayProperties {
    /**
     * websocket服务名
     */
    private String wsService;
    /**
     * passport session center接口
     */
    private String passportSessionUrl;

}
