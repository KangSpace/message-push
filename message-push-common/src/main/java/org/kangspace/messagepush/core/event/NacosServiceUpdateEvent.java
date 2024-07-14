package org.kangspace.messagepush.core.event;

import org.springframework.context.ApplicationEvent;

/**
 * Nacos服务更新事件
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/2
 */
public class NacosServiceUpdateEvent<T> extends ApplicationEvent {
    public NacosServiceUpdateEvent(T source) {
        super(source);
    }
}
