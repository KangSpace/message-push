package org.kangspace.messagepush.consumer.core.mq.kafka;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka 通道绑定
 *
 * @author kango2gler@gmail.com
 * @since 2021/09/03
 */
@Configuration
@EnableBinding(value = MessagePushChannel.class)
public class KafkaBindingConfig {
}
