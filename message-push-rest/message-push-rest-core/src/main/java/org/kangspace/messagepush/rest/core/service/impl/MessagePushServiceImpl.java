package org.kangspace.messagepush.rest.core.service.impl;


import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestDTO;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.kangspace.messagepush.rest.core.constant.AppThreadLocal;
import org.kangspace.messagepush.rest.core.mq.kafka.KafkaMqSender;
import org.kangspace.messagepush.rest.core.mq.kafka.MessagePushChannel;
import org.kangspace.messagepush.rest.core.service.BaseService;
import org.kangspace.messagepush.rest.core.service.MessagePushService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 消息推送Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Service
public class MessagePushServiceImpl extends BaseService implements MessagePushService {
    @Resource
    private KafkaMqSender kafkaMqSender;

    @Override
    public ApiResponse messageHandle(MessagePushRequestDTO messagePushRequestDto, HttpServletRequest request) {
        // 构建带时间的消息对象
        MessagePushRequestTimeDTO messagePushRequestTimeDto = MessagePushRequestTimeDTO.build(messagePushRequestDto);
        messagePushRequestTimeDto.setAppKey(AppThreadLocal.getAppKey());
        // 发送Kafka
        boolean isSendSucceed = kafkaMqSender.send(MessagePushChannel.OUTPUT_MESSAGE_PUSH_SINGLE_TOPIC, messagePushRequestTimeDto);
        // 返回处理
        ApiResponse response = new ApiResponse(isSendSucceed ? ResponseEnum.OK : ResponseEnum.INTERNAL_SERVER_ERROR);
        return response;
    }
}
