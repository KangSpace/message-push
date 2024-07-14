package org.kangspace.messagepush.ws.gateway.util;

import io.netty.handler.codec.http.HttpScheme;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.util.HttpUtils;
import org.kangspace.messagepush.core.util.IpUtils;
import org.kangspace.messagepush.ws.gateway.model.MessageRequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

/**
 * Exchange请求工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
public class ExchangeRequestUtils {

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
                && (HttpScheme.HTTP.toString().equals(scheme) || HttpScheme.HTTPS.toString().equals(scheme));
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

    /**
     * 获取消息请求参数
     * 参数: {@link MessagePushConstants#HTTP_HEADER_AUTH_APP_ID_KEY},{@link HttpHeaders#AUTHORIZATION}
     * 1. 从请求头中获取
     * 2. 从URL中获取
     *
     * @param exchange {@link ServerWebExchange}
     * @return {@link MessageRequestParam}
     */
    public static MessageRequestParam getMessageRequestParam(ServerWebExchange exchange) {
        MessageRequestParam requestParam = exchange.getAttribute(MessagePushConstants.EXCHANGE_ATTR_REQUEST_PARAM);
        // 从请求头中获取参数
        if (requestParam == null) {
            requestParam = getMessageRequestParamFromHeader(exchange);
        }
        // 从URL中获取参数
        if (requestParam == null) {
            requestParam = getMessageRequestParamFromUrl(exchange);
        }
        return requestParam != null ? requestParam : new MessageRequestParam();
    }

    /**
     * 从Url中获取认证信息
     *
     * @param exchange {@link ServerWebExchange}
     * @return @link MessageRequestParam}
     */
    public static MessageRequestParam getMessageRequestParamFromUrl(ServerWebExchange exchange) {
        String token = exchange.getRequest().getQueryParams().getFirst(HttpHeaders.AUTHORIZATION);
        boolean hasToken = StringUtils.hasText(token);
        if (hasToken) {
            token = token.startsWith(HttpUtils.HTTP_BEARER_TOKEN_VALUE_PREFIX) ? token.substring(HttpUtils.HTTP_BEARER_TOKEN_VALUE_PREFIX.length()) : token;
        }
        String authAppId = exchange.getRequest().getQueryParams().getFirst(MessagePushConstants.HTTP_HEADER_AUTH_APP_ID_KEY);
        return hasToken && StringUtils.hasText(authAppId) ? new MessageRequestParam(token, authAppId, MessageRequestParam.ParamFrom.URL) : null;
    }

    /**
     * 从请求头中获取认证信息
     *
     * @param exchange
     * @return
     */
    public static MessageRequestParam getMessageRequestParamFromHeader(ServerWebExchange exchange) {
        String token = HttpUtils.getHttpBearerToken(exchange.getRequest());
        String authAppId = exchange.getRequest().getHeaders().getFirst(MessagePushConstants.HTTP_HEADER_AUTH_APP_ID_KEY);
        return StringUtils.hasText(token) && StringUtils.hasText(authAppId) ? new MessageRequestParam(token, authAppId, MessageRequestParam.ParamFrom.HEADER) : null;
    }

    /**
     * 设置消息请求参数
     *
     * @param exchange {@link ServerWebExchange}
     * @return {@link MessageRequestParam}
     */
    public static void setMessageRequestParam(ServerWebExchange exchange, MessageRequestParam param) {
        exchange.getAttributes().put(MessagePushConstants.EXCHANGE_ATTR_REQUEST_PARAM, param);
    }

}
