package org.kangspace.messagepush.ws.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.ws.gateway.filter.balancer.LbServiceInstanceChooser;
import org.kangspace.messagepush.ws.gateway.filter.balancer.WebSocketLoadBalancer;
import org.kangspace.messagepush.ws.gateway.model.MessageRequestParam;
import org.kangspace.messagepush.ws.gateway.util.ExchangeRequestUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

/**
 * <pre>
 * WebSocket路由负载均衡过滤器
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
@Slf4j
public class WebsocketReactiveLoadBalancerClientFilter extends ReactiveLoadBalancerClientFilter implements GlobalFilter, Ordered, BeanPostProcessor {

    private final LbServiceInstanceChooser lbServiceInstanceChooser;

    private final LoadBalancerClientFactory clientFactory;

    private final LoadBalancerProperties properties;

    public WebsocketReactiveLoadBalancerClientFilter(LbServiceInstanceChooser lbServiceInstanceChooser,
                                                     LoadBalancerClientFactory clientFactory,
                                                     LoadBalancerProperties properties) {
        super(clientFactory, properties);
        this.lbServiceInstanceChooser = lbServiceInstanceChooser;
        this.clientFactory = clientFactory;
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().toString();
        MessageRequestParam requestParam = ExchangeRequestUtils.getMessageRequestParam(exchange);
        log.info("WebSocket路由负载均衡过滤器: WebsocketReactiveLoadBalancerClientFilter handle start, url: [{}], requestParam: [{}]",
                requestPath, requestParam);
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        Mono<Void> result;
        boolean isLbUrl = url != null && ("lb".equals(url.getScheme()) || "lb".equals(schemePrefix));
        if (isLbUrl) {
            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, url);
            if (log.isTraceEnabled()) {
                log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName() + " url before: " + url);
            }

            result = this.choose(exchange).doOnNext((response) -> {
                if (!response.hasServer()) {
                    throw NotFoundException.create(this.properties.isUse404(), "Unable to find instance for " + url.getHost());
                } else {
                    URI uri = exchange.getRequest().getURI();
                    String overrideScheme = null;
                    if (schemePrefix != null) {
                        overrideScheme = url.getScheme();
                    }

                    DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(response.getServer(), overrideScheme);
                    URI requestUrl = this.reconstructURI(serviceInstance, uri);
                    if (log.isTraceEnabled()) {
                        log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
                    }
                    String serviceNode = serviceInstance.getHost() + ":" + serviceInstance.getPort();
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);
                    exchange.getAttributes().put(MessagePushConstants.WEBSOCKET_TARGET_SERVICE_NODE_ATTR_KEY, serviceNode);
                    log.info("WebSocket路由负载均衡过滤器: WebsocketReactiveLoadBalancerClientFilter handle end, 目标服务:[{}], url: [{}], requestParam: [{}]", serviceNode, requestPath, requestParam);
                }
            }).then(chain.filter(exchange));
        } else {
            result = chain.filter(exchange);
            log.info("WebSocket路由负载均衡过滤器: 非负载请求,直连服务");
        }

        return result;
    }

    /**
     * 重新设置URI的service info
     *
     * @param serviceInstance 目标服务信息
     * @param original        源请求URL
     * @return {@link URI}
     */
    @Override
    protected URI reconstructURI(ServiceInstance serviceInstance, URI original) {
        return LoadBalancerUriTools.reconstructURI(serviceInstance, original);
    }

    /**
     * 选择路由Server实例
     *
     * @param exchange {@link ServerWebExchange}
     * @return {@link Mono}
     */
    private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        Objects.requireNonNull(uri, "ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR attr not found!");
        String serviceId = exchange.getRequest().getPath().contextPath().toString();
        WebSocketLoadBalancer loadBalancer = new WebSocketLoadBalancer(
                serviceId,
                clientFactory.getLazyProvider(uri.getHost(), ServiceInstanceListSupplier.class),
                this.lbServiceInstanceChooser);
        return loadBalancer.choose(new DefaultRequest<>(exchange));
    }

    @Override
    public int getOrder() {
        return FilterOrders.WEBSOCKET_REACTIVE_LOADBALANCER_CLIENT_FILTER_ORDER;
    }

}
