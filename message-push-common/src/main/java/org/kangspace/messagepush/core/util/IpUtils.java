package org.kangspace.messagepush.core.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.function.Supplier;

/**
 * Ip相关工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
public class IpUtils {

    public static final String UNKNOWN = "unknown";
    public static final String COMMA = ",";
    public static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";
    public static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HEADER_HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    public static final String HEADER_HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    public static final String HEADER_X_REAL_IP = "X-Real-IP";

    /**
     * 获取客户端Ip
     *
     * @return string
     */
    public static String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst(HEADER_X_FORWARDED_FOR);
        if (ip != null && ip.length() != 0 && !UNKNOWN.equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(COMMA)) {
                ip = ip.split(",")[0];
            }
        }
        ip = getIp(request, ip, HEADER_PROXY_CLIENT_IP, HEADER_WL_PROXY_CLIENT_IP);
        ip = getIp(request, ip, HEADER_HTTP_CLIENT_IP, HEADER_HTTP_X_FORWARDED_FOR);
        ip = getIp(ip, () -> request.getHeaders().getFirst(HEADER_X_REAL_IP));
        ip = getIp(ip, () -> request.getRemoteAddress() != null ? request.getRemoteAddress().getHostString() : "");
        return ip;
    }

    /**
     * 获取HEADER_WL_PROXY_CLIENT_IP,HEADER_HTTP_X_FORWARDED_FOR类型的IP
     *
     * @param request               request
     * @param ip                    当前IP
     * @param headerProxyClientIp   {@link #HEADER_WL_PROXY_CLIENT_IP}
     * @param headerWlProxyClientIp {@link #HEADER_HTTP_X_FORWARDED_FOR}
     * @return 新IP
     */
    private static String getIp(ServerHttpRequest request, String ip, String headerProxyClientIp, String headerWlProxyClientIp) {
        ip = getIp(ip, () -> request.getHeaders().getFirst(headerProxyClientIp));
        ip = getIp(ip, () -> request.getHeaders().getFirst(headerWlProxyClientIp));
        return ip;
    }

    /**
     * 获取IP
     *
     * @param ip       当前IP
     * @param supplier 提供获取IP的方法
     * @return 新IP值
     */
    private static String getIp(String ip, Supplier<String> supplier) {
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            return supplier.get();
        }
        return ip;
    }
}
