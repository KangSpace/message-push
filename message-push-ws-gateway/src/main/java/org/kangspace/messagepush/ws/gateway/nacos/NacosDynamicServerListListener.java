package org.kangspace.messagepush.ws.gateway.nacos;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.event.NacosServiceUpdateEvent;
import org.kangspace.messagepush.core.event.NacosServiceUpdateInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;

/**
 * Nacos动态服务列表监听
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/1
 */
@Slf4j
public class NacosDynamicServerListListener implements ApplicationContextAware, ApplicationEventPublisher {
    private final NacosDiscoveryProperties properties;
    private ApplicationContext applicationContext;
    private String serviceId;

    public NacosDynamicServerListListener(NacosDiscoveryProperties properties, String serviceId) {
        this.properties = properties;
        this.serviceId = serviceId;
        startListen();
    }

    /**
     * 开始监听
     */
    public void startListen() {
        log.info("Nacos服务动态监听: 开始监听,服务名:[{}]", serviceId);
        try {
            this.properties.namingServiceInstance().subscribe(serviceId, Arrays.asList(properties.getClusterName()), event -> {
                log.info("Nacos服务动态监听: 服务变更事件,服务名:[{}],事件:[{}]", serviceId, event);
                if (event instanceof NamingEvent) {
                    NamingEvent namingEvent = (NamingEvent) event;
                    this.publishEvent(new NacosServiceUpdateEvent<>(new NacosServiceUpdateInfo(serviceId, namingEvent.getInstances())));
                }
            });
        } catch (NacosException e) {
            log.error("Nacos服务动态监听: 订阅服务变更事件异常, 错误信息:[{}]", e.getMessage(), e);
        }
    }

    /**
     * 发布服务变化事件
     *
     * @param serviceChangeEvent 服务变化事件
     */
    public void publishEvent(NacosServiceUpdateEvent<NacosServiceUpdateInfo> serviceChangeEvent) {
        if (serviceChangeEvent != null) {
            log.info("Nacos服务动态监听: 发布服务更新事件, instanceDigest: [{}]",
                    ((NacosServiceUpdateInfo) serviceChangeEvent.getSource()).getInstancesDigest());
            this.publishEvent((Object) serviceChangeEvent);
        }
    }

    /**
     * 发布服务变化事件
     *
     * @param event 事件对象
     */
    @Override
    public void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
