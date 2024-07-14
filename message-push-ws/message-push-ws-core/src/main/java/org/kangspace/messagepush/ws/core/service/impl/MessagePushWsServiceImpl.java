package org.kangspace.messagepush.ws.core.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.kangspace.messagepush.ws.core.domain.model.HttpPushMessageDTO;
import org.kangspace.messagepush.ws.core.service.BaseService;
import org.kangspace.messagepush.ws.core.service.MessagePushWsService;
import org.kangspace.messagepush.ws.core.websocket.HttpPushMessagePublisher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 消息推送Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Slf4j
@Service
public class MessagePushWsServiceImpl extends BaseService implements MessagePushWsService {

    @Resource
    private HttpPushMessagePublisher httpPushMessagePublisher;

    @Override
    public ApiResponse messagePush(MessagePushRequestTimeDTO messagePushRequestDto) {
        log.info("Http消息推送信息处理: 收到Http推送请求, message:[{}]", messagePushRequestDto);
        // 处理消息
        boolean state = httpPushMessagePublisher.publish(HttpPushMessageDTO.from(messagePushRequestDto));
        ApiResponse result = state ? new ApiResponse(ResponseEnum.OK) : new ApiResponse(ResponseEnum.INTERNAL_SERVER_ERROR.getValue(), "推送数据失败!");
        log.info("Http消息推送信息处理: Http推送请求处理结束, result:[{}] message:[{}]", state, messagePushRequestDto);
        return result;
    }
}
