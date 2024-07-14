package org.kangspace.messagepush.consumer;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kangspace.messagepush.consumer.core.config.ElasticSearchIndexInitial;
import org.kangspace.messagepush.consumer.core.domain.dto.request.MessagePushRequestDto;
import org.kangspace.messagepush.consumer.core.service.ElasticSearchService;
import org.kangspace.messagepush.consumer.core.service.MessagePushConsumerService;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * @author kango2gler@gmail.com
 * @since 2021/8/10
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MessagePushConsumerApplication.class)
public class MessagePushServiceTest {
    @Resource
    private ElasticSearchIndexInitial elasticSearchIndexInitial;
    @Resource
    private MessagePushConsumerService messagePushConsumerService;
    @Resource
    private ElasticSearchService elasticSearchService;

    @Test
    public void temp() {
    }


    /**
     * ES插入测试
     */
    @Test
    public void esInsert() {
        log.info("::: ES插入测试:");
        String index = elasticSearchIndexInitial.getMessagePushSingleTopicIndex();
        MessagePushRequestTimeDTO dto = newMessagePushRequestTimeDTO();
        boolean inserted = elasticSearchService.insert(index, dto);
        Assert.assertTrue("ES插入测试,数据插入失败", inserted);
        //查询日志信息
        MessagePushRequestTimeDTO responseDto = queryEsData(dto.getMessageId());
        Assert.assertTrue("ES插入测试,数据查询失败,数据为空", responseDto != null);
    }


    /**
     * 消息消费测试
     */
    @Test
    public void messageHandleTest() {
        log.info("::: 消息消费测试:");
        MessagePushRequestTimeDTO dto = newMessagePushRequestTimeDTO();
        String message = JSONObject.toJSONString(dto);
        messagePushConsumerService.messageHandle(message);
        //查询日志信息
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //查询日志信息
        MessagePushRequestTimeDTO responseDto = queryEsData(dto.getMessageId());
        Assert.assertTrue("ES插入测试,数据查询失败,数据为空", responseDto != null);
    }

    /**
     * 获取ES查询结果
     */
    @Test
    public void queryEsDataTest() {
        String messageId = "f58889e5fce64b8fa2de1da26f69ce68";
        MessagePushRequestTimeDTO dto = queryEsData(messageId);
        System.out.println(dto);
    }

    /**
     * 获取ES查询结果
     *
     * @param messageId 消息ID
     * @return {@link MessagePushRequestTimeDTO}
     */
    public MessagePushRequestTimeDTO queryEsData(String messageId) {
        String index = elasticSearchIndexInitial.getMessagePushSingleTopicIndex();
        Order defaultOrder = Order.builder().field("c_time").sequence(-1).build();
        MessagePushRequestDto requestDto = new MessagePushRequestDto();
        requestDto.setMessageId(messageId);
        return Optional.ofNullable(
                elasticSearchService.page(index, requestDto, defaultOrder, MessagePushRequestTimeDTO.class)
        ).map(t -> t.getList()).orElseGet(ArrayList::new).stream().findFirst().orElse(null);
    }

    /**
     * 获取测试的newMessagePushRequestTimeDTO
     *
     * @return
     */
    public MessagePushRequestTimeDTO newMessagePushRequestTimeDTO() {
        String message = "{\"push_method\":1,\"platform\":\"\",\"audience\":{\"alias\":[\"alias1\",\"alias2\"]},\"message\":{\"title\":\"title1\",\"content\":\"content1\",\"content_type\":\"type1\",\"extras\":\"\"}}";
        MessagePushRequestTimeDTO dto = JSONObject.parseObject(message, MessagePushRequestTimeDTO.class);
        dto.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        dto.setCTime(new LocalDateTime().toDate());
        return dto;
    }
}
