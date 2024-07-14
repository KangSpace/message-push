package org.kangspace.messagepush.ws.controller;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.kangspace.messagepush.ws.core.service.MessagePushWsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 消息推送REST API接口
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
@Validated
@RestController
@RequestMapping(value = "v1/inner/push")
public class MessagePushRestController {

    @Resource
    private MessagePushWsService messagePushWsService;

    /**
     * 消息推送PUSH接口
     *
     * @param messagePushRequestDto MessagePushRequestTimeDTO
     * @return ApiResponse
     */
    @PostMapping("")
    public ApiResponse messagePush(@Validated @RequestBody MessagePushRequestTimeDTO messagePushRequestDto) {
        return messagePushWsService.messagePush(messagePushRequestDto);
    }
}
