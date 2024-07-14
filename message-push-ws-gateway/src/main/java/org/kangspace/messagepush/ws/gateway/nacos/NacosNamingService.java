package org.kangspace.messagepush.ws.gateway.nacos;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;

import java.util.Collections;
import java.util.List;

/**
 * NacosNamingService 实例
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/2
 */
@Data
@Slf4j
public class NacosNamingService {
    private final NacosServiceDiscovery nacosServiceDiscovery;

    public NacosNamingService(NacosServiceDiscovery nacosServiceDiscovery) {
        this.nacosServiceDiscovery = nacosServiceDiscovery;
    }

    /**
     * 获取所有服务实例
     *
     * @return List<ServiceInstance>
     */
    public List<ServiceInstance> getAllInstances(String serviceId) {
        try {
            return this.nacosServiceDiscovery.getInstances(serviceId);
        } catch (NacosException e) {
            log.error("Nacos服务动态监听: 获取所有实例错误, 错误信息:[{}]", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

}
