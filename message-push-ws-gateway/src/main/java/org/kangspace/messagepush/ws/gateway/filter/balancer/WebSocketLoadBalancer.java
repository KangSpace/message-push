package org.kangspace.messagepush.ws.gateway.filter.balancer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * WebSocket服务负载均衡器
 *
 * @author kango2gler@gmail.com
 * @see RoundRobinLoadBalancer
 * @see LbServiceInstanceChooser
 * @since 2021/10/27
 */
@Slf4j
public class WebSocketLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private final String serviceId;
    /**
     * 服务实例列表提供者
     */
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    /**
     * 服务实例选择器
     */
    private LbServiceInstanceChooser instanceChooser;

    public WebSocketLoadBalancer(String serviceId,
                                 ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                 LbServiceInstanceChooser instanceChooser) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.instanceChooser = instanceChooser;
    }

    /**
     * 路由服务选择
     *
     * @param request {@link Request}
     * @return {@link Mono<Response<ServiceInstance>>}
     */
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return ((Flux) supplier.get()).next().map(instances -> this.getInstanceResponse((List<ServiceInstance>) instances, request));
    }

    /**
     * 从服务列表中选择目标服务
     *
     * @param instances 服务列表
     * @param request   request
     * @return {@link Response<ServiceInstance>}
     */
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances, Request request) {
        ServiceInstance instance = this.instanceChooser.choose(this.serviceId, (ServerWebExchange) request.getContext(), instances);
        return instance != null ? new DefaultResponse(instance) : new EmptyResponse();
    }
}
