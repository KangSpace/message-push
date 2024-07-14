package org.kangspace.messagepush.core.util;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

/**
 * Exchange请求工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
public class ExchangeRequestUtils {
    private final static String HTTP = "http";
    private final static String HTTPS = "http";

    /**
     * <pre>
     * 是否为Websocket请求
     * 协议为Http且Request Header: [Connection: Upgrade ,Upgrade: WebSocket]
     * </pre>
     *
     * @param exchange {@link ServerWebExchange}
     * @return boolean
     */
    public static boolean isWebsocketRequest(ServerWebExchange exchange) {
        String scheme = exchange.getRequest().getURI().getScheme().toLowerCase();
        String connection = exchange.getRequest().getHeaders().getConnection().stream().findFirst().orElse(null);
        String upgrade = exchange.getRequest().getHeaders().getUpgrade();
        boolean isWebSocketRequest = "WebSocket".equalsIgnoreCase(upgrade)
                && HttpHeaders.UPGRADE.equalsIgnoreCase(connection)
                && (HTTP.equals(scheme) || HTTPS.equals(scheme));
        return isWebSocketRequest;
    }

    /**
     * <pre>
     * 获取客户端Ip地址
     * </pre>
     *
     * @param exchange {@link ServerWebExchange}
     * @return ip
     */
    public static String getIP(ServerWebExchange exchange) {
        return IpUtils.getClientIp(exchange.getRequest());
    }
}
