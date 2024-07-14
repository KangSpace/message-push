package org.kangspace.messagepush.ws.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.ws.gateway.filter.session.SessionProxyHolder;
import org.kangspace.messagepush.ws.gateway.filter.session.WebSocketUserSessionManager;
import org.kangspace.messagepush.ws.gateway.model.MessageRequestParam;
import org.kangspace.messagepush.ws.gateway.util.ExchangeRequestUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.WebsocketRoutingFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义WebsocketRoutingFilter
 *
 * @author kango2gler@gmail.com
 * @see WebsocketRoutingFilter
 * @since 2021/11/4
 */
@Slf4j
public class GatewayWebsocketRoutingFilter extends BaseFilter implements GlobalFilter, Ordered {
    public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
    private static final String HTTP_SCHEMA = "http";
    private static final String HTTPS_SCHEMA = "https";
    private static final String WS_SCHEMA = "ws";
    private static final String WSS_SCHEMA = "wss";
    private static final String WEBSOCKET_UPGRADE = "Websocket";
    private final WebSocketClient webSocketClient;
    private final WebSocketService webSocketService;
    private final ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider;
    private final WebSocketUserSessionManager webSocketUserSessionManager;
    private volatile List<HttpHeadersFilter> headersFilters;

    public GatewayWebsocketRoutingFilter(WebSocketClient webSocketClient, WebSocketService webSocketService,
                                         ObjectProvider<List<HttpHeadersFilter>> headersFiltersProvider,
                                         WebSocketUserSessionManager webSocketUserSessionManager) {
        this.webSocketClient = webSocketClient;
        this.webSocketService = webSocketService;
        this.headersFiltersProvider = headersFiltersProvider;
        this.webSocketUserSessionManager = webSocketUserSessionManager;
    }

    /**
     * 协议转换
     *
     * @param scheme 请求的Scheme
     * @return websocket协议
     */
    static String convertHttpToWs(String scheme) {
        scheme = scheme.toLowerCase();
        return HTTP_SCHEMA.equals(scheme) ? WS_SCHEMA : (HTTPS_SCHEMA.equals(scheme) ? WSS_SCHEMA : scheme);
    }

    @Override
    public int getOrder() {
        return FilterOrders.WEBSOCKET_ROUTING_FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        this.changeSchemeIfIsWebSocketUpgrade(exchange);
        URI requestUrl = (URI) exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String scheme = requestUrl.getScheme();
        if (!ServerWebExchangeUtils.isAlreadyRouted(exchange) && (WS_SCHEMA.equals(scheme) || WSS_SCHEMA.equals(scheme))) {
            ServerWebExchangeUtils.setAlreadyRouted(exchange);
            HttpHeaders headers = exchange.getRequest().getHeaders();
            HttpHeaders filtered = HttpHeadersFilter.filterRequest(this.getHeadersFilters(), exchange);
            List<String> protocols = headers.get(SEC_WEBSOCKET_PROTOCOL);
            if (protocols != null) {
                protocols = (List) headers.get(SEC_WEBSOCKET_PROTOCOL).stream()
                        .flatMap((header) -> Arrays.stream(StringUtils.commaDelimitedListToStringArray(header)))
                        .map(String::trim).collect(Collectors.toList());
            }
            return this.webSocketService.handleRequest(exchange,
                    new GatewayWebsocketRoutingFilter.ProxyWebSocketHandler(exchange, requestUrl, this.webSocketClient,
                            filtered, protocols, webSocketUserSessionManager));
        } else {
            return chain.filter(exchange);
        }
    }

    /**
     * 获取请求头过滤器
     *
     * @return
     */
    private List<HttpHeadersFilter> getHeadersFilters() {
        if (this.headersFilters == null) {
            this.headersFilters = (List) this.headersFiltersProvider.getIfAvailable(ArrayList::new);
            this.headersFilters.add((headers, exchange) -> {
                HttpHeaders filtered = new HttpHeaders();
                headers.entrySet().stream().filter((entry) -> !entry.getKey().toLowerCase().startsWith("sec-websocket"))
                        .forEach((header) -> filtered.addAll(header.getKey(), header.getValue()));
                return filtered;
            });
        }

        return this.headersFilters;
    }

    /**
     * Websocket协议升级
     *
     * @param exchange
     */
    private void changeSchemeIfIsWebSocketUpgrade(ServerWebExchange exchange) {
        URI requestUrl = (URI) exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String scheme = requestUrl.getScheme().toLowerCase();
        String upgrade = exchange.getRequest().getHeaders().getUpgrade();
        if (WEBSOCKET_UPGRADE.equalsIgnoreCase(upgrade) && (HTTP_SCHEMA.equals(scheme) || HTTPS_SCHEMA.equals(scheme))) {
            String wsScheme = convertHttpToWs(scheme);
            URI wsRequestUrl = UriComponentsBuilder.fromUri(requestUrl).scheme(wsScheme).build().toUri();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, wsRequestUrl);
            if (log.isTraceEnabled()) {
                log.trace("changeSchemeTo:[" + wsRequestUrl + "]");
            }
        }

    }

    /**
     * 网关代理Websocket处理类
     * 与后端服务请求交互的核心类
     */
    private static class ProxyWebSocketHandler implements WebSocketHandler {
        private final ServerWebExchange exchange;
        private final WebSocketClient client;
        private final URI url;
        private final HttpHeaders headers;
        private final List<String> subProtocols;
        private final WebSocketUserSessionManager webSocketUserSessionManager;

        ProxyWebSocketHandler(ServerWebExchange exchange, URI url, WebSocketClient client, HttpHeaders headers,
                              List<String> protocols, WebSocketUserSessionManager webSocketUserSessionManager) {
            this.exchange = exchange;
            this.client = client;
            this.url = url;
            this.headers = headers;
            this.webSocketUserSessionManager = webSocketUserSessionManager;
            if (protocols != null) {
                this.subProtocols = protocols;
            } else {
                this.subProtocols = Collections.emptyList();
            }
        }

        @Override
        public List<String> getSubProtocols() {
            return this.subProtocols;
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {
            return this.client.execute(this.url, this.headers, new WebSocketHandler() {
                @Override
                public Mono<Void> handle(WebSocketSession proxySession) {
                    Mono<Void> proxySessionSend = proxySession.send(session.receive().doOnNext(WebSocketMessage::retain));
                    Mono<Void> serverSessionSend = session.send(proxySession.receive().doOnNext(WebSocketMessage::retain));
                    // 绑定用户Session关系
                    addUserSession(proxySession, session);
                    return Mono.zip(proxySessionSend, serverSessionSend).then();
                }

                @Override
                public List<String> getSubProtocols() {
                    return GatewayWebsocketRoutingFilter.ProxyWebSocketHandler.this.subProtocols;
                }
            }).doOnTerminate(() -> {
                // 移除用户Session关系
                removeUserSession();
            });
        }

        /**
         * 添加用户Session
         *
         * @param proxySession
         * @param session
         */
        private void addUserSession(WebSocketSession proxySession, WebSocketSession session) {
            MessageRequestParam param = ExchangeRequestUtils.getMessageRequestParam(exchange);
            String uid = param.getUid();
            String serviceNode = exchange.getAttribute(MessagePushConstants.WEBSOCKET_TARGET_SERVICE_NODE_ATTR_KEY);
            SessionProxyHolder sessionProxyHolder = new SessionProxyHolder(proxySession, session, serviceNode);
            exchange.getAttributes().put(MessagePushConstants.USER_SESSION_HOLDER_EXCHANGE_ATTR_KEY, sessionProxyHolder);
            // 绑定用户Session和双向Session信息
            webSocketUserSessionManager.addSession(uid, sessionProxyHolder);
            log.info("Websocket路由: 用户已连接, uid:[{}],session:[{}],目标服务:[{}]", uid, session.getId(), serviceNode);
        }

        /**
         * 删除用户Session信息
         */
        private void removeUserSession() {
            MessageRequestParam param = ExchangeRequestUtils.getMessageRequestParam(exchange);
            String uid = param.getUid();
            SessionProxyHolder sessionProxyHolder = exchange.getAttribute(MessagePushConstants.USER_SESSION_HOLDER_EXCHANGE_ATTR_KEY);
            if (sessionProxyHolder != null) {
                webSocketUserSessionManager.removeSession(uid, sessionProxyHolder.getSession());
                log.info("Websocket路由: 用户已断开, uid:[{}],session:[{}],目标服务:[{}]", uid, sessionProxyHolder.getSession(), sessionProxyHolder.getProxySession());
            }
        }
    }
}
