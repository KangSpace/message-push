package org.kangspace.messagepush.consumer.core.service;

/**
 * 消息推送Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
public interface MessagePushConsumerService {
    /**
     * 消息处理
     *
     * @param message kafka的消息内容
     * @return boolean
     */
    boolean messageHandle(String message);
}
