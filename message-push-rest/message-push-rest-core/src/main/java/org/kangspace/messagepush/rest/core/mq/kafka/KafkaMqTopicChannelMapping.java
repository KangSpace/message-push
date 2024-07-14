package org.kangspace.messagepush.rest.core.mq.kafka;

import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Topic和Channel映射表
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Component
public class KafkaMqTopicChannelMapping {
    /**
     * Topic和MessageChannel映射
     */
    public Map<String, MessageChannel> topicMessageChannelMap = new HashMap<>();
    @Resource
    private MessagePushChannel messagePushChannel;

    /**
     * 注册Topic和MessageChannel映射关系
     */
    @PostConstruct
    public void init() {
        topicMessageChannelMap.put(MessagePushChannel.OUTPUT_MESSAGE_PUSH_SINGLE_TOPIC, messagePushChannel.outputMessagePushSingle());
    }

    /**
     * 通过Topic获取MessageChannel
     *
     * @param topic
     * @return
     */
    public MessageChannel getMessageChannel(String topic) {
        return topicMessageChannelMap.get(topic);
    }
}
