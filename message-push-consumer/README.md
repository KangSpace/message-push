# message-push-consumer

消息推送消费者服务

## 项目提供服务

1. 消费Kafka消息数据
2. 推送消息到message-push-ws服务

## 项目涉及中间件

    nacos(namespace):
        dev: kangspace_dev
    Kafka:
        topic: message_push_single_topic
        group: message-topic-default-consumer
        consumer.concurrency: 1
    ElasticSearch:
        index: message_push_single_topic_index