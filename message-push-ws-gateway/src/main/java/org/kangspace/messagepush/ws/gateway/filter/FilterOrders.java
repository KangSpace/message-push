package org.kangspace.messagepush.ws.gateway.filter;

/**
 * 过滤器Order常量类
 *
 * @author kango2gler@gmail.com
 * @date 2021/10/28
 */
public class FilterOrders {

    /**
     * 初始过滤器Order
     */
    public static final int DEFAULT_FILTER_ORDER = 10000;

    /**
     * {@link org.springframework.cloud.gateway.filter.WebsocketRoutingFilter} Order
     */
    public static final int WEBSOCKET_ROUTING_FILTER_ORDER = 2147483644;

    /**
     * 验证拦截器order
     */
    public static final int VALIDATE_FILTER_ORDER = DEFAULT_FILTER_ORDER + 1;

    /**
     * 请求路由过滤器order
     *
     * @see org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter
     */
    public static final int WEBSOCKET_REACTIVE_LOADBALANCER_CLIENT_FILTER_ORDER = DEFAULT_FILTER_ORDER + 101;

}
