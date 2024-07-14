package org.kangspace.messagepush.rest.core.service;


import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestDTO;

import javax.servlet.http.HttpServletRequest;

/**
 * 消息推送Service
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
public interface MessagePushService {
    /**
     * 消息处理
     *
     * @param messagePushRequestDto 消息请求Dto
     * @param request               HttpServletRequest
     * @return ApiResponse
     */
    ApiResponse messageHandle(MessagePushRequestDTO messagePushRequestDto, HttpServletRequest request);
}
