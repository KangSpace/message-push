package org.kangspace.messagepush.ws.core.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 消息推送Websocket握手处理服务
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Slf4j
public class MessagePushHandshakeWebSocketService extends HandshakeWebSocketService {
    public MessagePushHandshakeWebSocketService() {
        super();
    }

    public MessagePushHandshakeWebSocketService(RequestUpgradeStrategy upgradeStrategy) {
        super(upgradeStrategy);
    }

    @Override
    public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
        return super.handleRequest(exchange, handler);
    }
}
