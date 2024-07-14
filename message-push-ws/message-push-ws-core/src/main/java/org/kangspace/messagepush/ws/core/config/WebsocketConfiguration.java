package org.kangspace.messagepush.ws.core.config;

import org.kangspace.messagepush.ws.core.websocket.HttpPushMessageConsumer;
import org.kangspace.messagepush.ws.core.websocket.HttpPushMessagePublisher;
import org.kangspace.messagepush.ws.core.websocket.MessagePushHandshakeWebSocketService;
import org.kangspace.messagepush.ws.core.websocket.MessagePushWebSocketHandler;
import org.kangspace.messagepush.ws.core.websocket.session.WebSocketSessionManager;
import org.kangspace.messagepush.ws.core.websocket.session.WebSocketUserSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Websocket配置
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@Configuration
public class WebsocketConfiguration {
    private final static String ALLOWED_ORIGIN_ALL = "*";

    /**
     * 用户Session管理器
     *
     * @return WebSocketSessionManager
     */
    @Bean
    public WebSocketSessionManager webSocketUserSessionManager() {
        return new WebSocketUserSessionManager();
    }

    /**
     * Http推送消息消费者
     *
     * @param webSocketSessionManager WebSocketSessionManager
     * @return HttpPushMessageConsumer
     */
    @Bean
    public HttpPushMessageConsumer httpPushMessagePublisher(WebSocketSessionManager webSocketSessionManager) {
        return new HttpPushMessageConsumer(webSocketSessionManager);
    }

    /**
     * Http推送消息发布者
     *
     * @return HttpPushMessagePublisher
     */
    @Bean
    public HttpPushMessagePublisher httpPushMessagePublisher(HttpPushMessageConsumer httpPushMessagePublisher) {
        return new HttpPushMessagePublisher(httpPushMessagePublisher);
    }

    /**
     * WebsocketHandler
     *
     * @param webSocketSessionManager WebSocketSessionManager
     * @return MessagePushWebSocketHandler
     */
    @Bean
    public MessagePushWebSocketHandler messagePushWebSocketHandler(WebSocketSessionManager webSocketSessionManager) {
        return new MessagePushWebSocketHandler(webSocketSessionManager);
    }


    /**
     * 注册Websocket处理器
     *
     * @param webSocketHandlers MessagePushWebSocketHandler处理器
     * @see MessagePushWebSocketHandler
     */
    @Bean
    public HandlerMapping websocketHandlerMapping(List<MessagePushWebSocketHandler> webSocketHandlers,
                                                  CorsConfigurationSource corsConfigurationSource) {
        Map<String, Object> handlerMap = webSocketHandlers.stream()
                .collect(Collectors.toMap(MessagePushWebSocketHandler::getEndpointPath, v -> v));
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(handlerMap);
        handlerMapping.setCorsConfigurationSource(corsConfigurationSource);
        handlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return handlerMapping;
    }

    /**
     * 注册WebSocketHandlerAdapter
     *
     * @return {@link WebSocketHandlerAdapter}
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    /**
     * 注册WebSocketService
     *
     * @return {@WebSocketService}
     * @see HandshakeWebSocketService
     */
    @Bean
    public WebSocketService webSocketService() {
        return new MessagePushHandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }

    /**
     * 注册CorsConfigurationSource
     *
     * @return {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfigurationSource corsConfigurationSource = exchange -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.addAllowedOrigin(ALLOWED_ORIGIN_ALL);
            return configuration;
        };
        return corsConfigurationSource;
    }
}
