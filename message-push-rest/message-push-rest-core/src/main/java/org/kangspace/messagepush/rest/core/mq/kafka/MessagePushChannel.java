package org.kangspace.messagepush.rest.core.mq.kafka;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * <pre>
 * 消息推送输出通道
 * 添加新的Kafka输出通道需要以下3个步骤:
 * 1. 在Nacos <code>spring.cloud.stream.bindings</code>下配置新的channel,如:
 *    <code>   message_push_single_topic:
 *                 destination: message_push_single_topic
 *                 content-type: text/plain
 *    </code>
 * 2. 在{@link MessagePushChannel}中添加 Topic和@Output配置,如{@link #OUTPUT_MESSAGE_PUSH_SINGLE_TOPIC}和 {@link #outputMessagePushSingle()}
 * 3. 在{@link KafkaMqTopicChannelMapping#init()} 中添加Topic和MessageChannel映射
 * </pre>
 *
 * @author kango2gler@gmail.com
 */
public interface MessagePushChannel {

    /**
     * 消息推送Kafka数据通道
     */
    String OUTPUT_MESSAGE_PUSH_SINGLE_TOPIC = "message_push_single_topic";

    /**
     * 消息推送Kafka数据通道
     *
     * @return {@link org.springframework.messaging.MessageChannel}
     */
    @Output(OUTPUT_MESSAGE_PUSH_SINGLE_TOPIC)
    MessageChannel outputMessagePushSingle();

}
