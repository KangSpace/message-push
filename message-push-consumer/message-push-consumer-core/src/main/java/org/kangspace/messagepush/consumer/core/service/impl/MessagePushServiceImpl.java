package org.kangspace.messagepush.consumer.core.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.consumer.core.config.ElasticSearchIndexInitial;
import org.kangspace.messagepush.consumer.core.feign.MessagePushWsApi;
import org.kangspace.messagepush.consumer.core.hash.HashRouterLoader;
import org.kangspace.messagepush.consumer.core.service.BaseService;
import org.kangspace.messagepush.consumer.core.service.ElasticSearchService;
import org.kangspace.messagepush.consumer.core.service.MessagePushConsumerService;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 消息推送Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Slf4j
@Service
public class MessagePushServiceImpl extends BaseService implements MessagePushConsumerService {

    @Resource
    private ElasticSearchIndexInitial elasticSearchIndexInitial;

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private MessagePushWsApi messagePushWsApi;

    @Resource
    private HashRouterLoader hashRouterLoader;

    @Override
    public boolean messageHandle(String message) {
        log.info("推送消息消费数据处理: start,message:[{}]", message);
        // 消息处理
        MessagePushRequestTimeDTO messagePushRequestTimeDto = JsonUtil.toObject(message, MessagePushRequestTimeDTO.class);
        // 保存消息到ES
        messageStore(messagePushRequestTimeDto);
        // 消息分发到ws
        messagePushToWs(messagePushRequestTimeDto);
        log.info("推送消息消费数据处理: end,message:[{}]", messagePushRequestTimeDto.getMessageId());
        return true;
    }

    /**
     * 消息推送到Websocket处理
     *
     * @param messageDto MessagePushRequestTimeDTO
     */
    private void messagePushToWs(MessagePushRequestTimeDTO messageDto) {
        List<String> targetUIds = messageDto.getAudience().getUids();
        // 计算各用户所在的服务节点
        Map<String, List<String>> nodeUIdsMap = targetUIds.stream()
                .map(uid -> new String[]{hashRouterLoader.getPhysicalNode(uid), uid})
                .collect(Collectors.toMap(arr -> arr[0], arr -> {
                    List list = new ArrayList<>();
                    list.add(arr[1]);
                    return list;
                }, (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                }));
        if (CollectionUtils.isEmpty(nodeUIdsMap)) {
            log.warn("推送消息到Websocket处理,查询服务节点为空,message:[{}]", messageDto);
            return;
        }
        AtomicInteger pushCount = new AtomicInteger(0);
        nodeUIdsMap.forEach((node, uIds) -> {
            MessagePushRequestTimeDTO dto = new MessagePushRequestTimeDTO();
            BeanUtils.copyProperties(messageDto, dto);
            dto.getAudience().setUids(uIds);
            //node 负载处理
            messagePushWsApi.messagePush(dto, node);
            pushCount.addAndGet(1);
        });
        log.warn("推送消息到Websocket处理,结束, 服务调用次数:[{}]", pushCount.get());
    }

    public void messageStore(MessagePushRequestTimeDTO messagePushRequestTimeDto) {
        String messagePushSingleTopicIndex = elasticSearchIndexInitial.getMessagePushSingleTopicIndex();
        // 写ElasticSearch
        elasticSearchService.writeElasticSearch(messagePushSingleTopicIndex, messagePushRequestTimeDto);
    }

}
