package org.kangspace.messagepush.ws.gateway.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自动配置类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
@Configuration
@Import(MessagePushWsGatewayConfiguration.class)
@AutoConfigureBefore(GatewayLoadBalancerClientAutoConfiguration.class)
@EnableConfigurationProperties(MessagePushWsGatewayProperties.class)
public class MessagePushWsGatewayAutoConfiguration {

}
