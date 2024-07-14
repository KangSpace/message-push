package org.kangspace.messagepush.consumer.core.mq.kafka;

import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.consumer.core.service.MessagePushConsumerService;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 消息推送数据消费
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
@Slf4j
@Component
public class MessagePushConsumer {
    @Resource
    private MessagePushConsumerService messagePushConsumerService;

    /**
     * 订阅kafka消息处理
     *
     * @param message Kafka消息
     */
    @StreamListener(target = MessagePushChannel.INPUT_MESSAGE_PUSH_SINGLE_TOPIC)
    public void handle(@Payload String message) {
        log.info("消费Kafka消息: received a message from [{}]: {}", MessagePushChannel.INPUT_MESSAGE_PUSH_SINGLE_TOPIC, message);
        boolean result = false;
        try {
            result = messagePushConsumerService.messageHandle(message);
        } catch (Exception e) {
            log.error("消费Kafka消息: consumer message error:{}", e.getMessage(), e);
        }
        log.info("消费Kafka消息: consumer message result :[{}], message:[{}]", result, message);
    }
}
