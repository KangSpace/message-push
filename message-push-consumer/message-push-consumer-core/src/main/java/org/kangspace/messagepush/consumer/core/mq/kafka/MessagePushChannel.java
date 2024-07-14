package org.kangspace.messagepush.consumer.core.mq.kafka;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 消息推送Topic通道
 *
 * @author kango2gler@gmail.com
 */
public interface MessagePushChannel {

    /**
     * 消息推送Kafka数据通道
     */
    String INPUT_MESSAGE_PUSH_SINGLE_TOPIC = "message_push_single_topic";


    /**
     * 消息推送Kafka数据通道订阅
     *
     * @return {@link MessageChannel}
     */
    @Input(INPUT_MESSAGE_PUSH_SINGLE_TOPIC)
    SubscribableChannel inputMessagePushSingle();

}
