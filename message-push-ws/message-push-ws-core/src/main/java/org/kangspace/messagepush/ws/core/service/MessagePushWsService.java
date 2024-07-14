package org.kangspace.messagepush.ws.core.service;


import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;


/**
 * 消息推送Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
public interface MessagePushWsService {
    /**
     * 消息推送处理
     *
     * @param messagePushRequestDto messagePushRequestDto
     * @return ApiResponse
     */
    ApiResponse messagePush(MessagePushRequestTimeDTO messagePushRequestDto);
}
