package org.kangspace.messagepush.rest.core.mq.kafka;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kafka消息生产者
 *
 * @author kango2gler@gmail.com
 * @see KafkaMqTopicChannelMapping
 */
@Slf4j
@Component
public class KafkaMqSender {
    @Resource
    private KafkaMqTopicChannelMapping kafkaMqTopicChannelMapping;

    /**
     * 发送kafka消息到topic
     *
     * @param message message
     * @see KafkaMqTopicChannelMapping
     */
    public boolean send(String topic, Object message) {
        String msg = JsonUtil.toJson(message);
        log.info("发送Kafka消息: kafka send msg [begin], topic:{}, msg:[{}]", topic, msg);
        Message<String> messageBuild = MessageBuilder.withPayload(msg).build();
        MessageChannel messageChannel = kafkaMqTopicChannelMapping.getMessageChannel(topic);
        boolean isSendSucceed = false;
        if (messageChannel != null) {
            isSendSucceed = messageChannel.send(messageBuild);
        } else {
            log.error("发送Kafka消息: topic [{}] <=> MessageChannel is not exist in KafkaMqTopicChannelMapping !", topic);
        }
        log.info("发送Kafka消息: kafka send msg [{}], topic:[{}], msg:[{}]", isSendSucceed, topic, msg);
        return isSendSucceed;
    }

}
