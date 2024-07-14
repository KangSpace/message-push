package org.kangspace.messagepush.ws.gateway.filter.balancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

/**
 * 服务实例选择器
 *
 * @author kango2gler@gmail.com
 * @see UIDServiceInstanceChooser
 * @see RoundRibbonServiceInstanceChooser
 * @since 2021/10/27
 */
public interface LbServiceInstanceChooser {

    /**
     * 服务实例选择
     *
     * @param serviceId 服务ID
     * @param exchange  {@link ServerWebExchange}
     * @param instances 实例列表
     * @return ServiceInstance
     */
    ServiceInstance choose(String serviceId, ServerWebExchange exchange, List<ServiceInstance> instances);
}
