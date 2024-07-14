package org.kangspace.messagepush.ws.gateway.filter.balancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮训选择服务实例
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
@Slf4j
public class RoundRibbonServiceInstanceChooser implements LbServiceInstanceChooser {
    /**
     * 轮训计数器
     */
    private final AtomicInteger position;

    public RoundRibbonServiceInstanceChooser() {
        this(new Random().nextInt(1000));
    }

    public RoundRibbonServiceInstanceChooser(int seedPosition) {
        this.position = new AtomicInteger(seedPosition);
    }

    @Override
    public ServiceInstance choose(String serviceId, ServerWebExchange exchange, List<ServiceInstance> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            log.warn("轮训服务实例选择: No servers available for service: " + serviceId);
            return null;
        }
        int pos = Math.abs(this.position.incrementAndGet());
        return instances.get(pos % instances.size());
    }
}
