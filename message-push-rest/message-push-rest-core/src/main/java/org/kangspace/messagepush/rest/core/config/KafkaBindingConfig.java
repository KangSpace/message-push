package org.kangspace.messagepush.rest.core.config;

import org.kangspace.messagepush.rest.core.mq.kafka.MessagePushChannel;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
@Configuration
@EnableBinding(value = MessagePushChannel.class)
public class KafkaBindingConfig {
}
