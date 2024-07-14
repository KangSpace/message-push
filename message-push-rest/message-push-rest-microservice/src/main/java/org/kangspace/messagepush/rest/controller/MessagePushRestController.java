package org.kangspace.messagepush.rest.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.util.ObjectUtil;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestDTO;
import org.kangspace.messagepush.rest.core.auth.ApiAuthentication;
import org.kangspace.messagepush.rest.core.constant.MessagePushConstants;
import org.kangspace.messagepush.rest.core.service.MessagePushService;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 消息推送REST API接口
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
@Validated
@RestController
@Api(value = "消息推送REST API接口", tags = {"消息推送REST API"})
@RequestMapping(value = "v1/push")
public class MessagePushRestController {

    @Resource
    private MessagePushService messagePushService;

    /**
     * 消息推送PUSH接口
     *
     * @param messagePushRequestDto
     * @param request
     * @return
     */
    @ApiAuthentication
    @ApiOperation(value = "消息推送PUSH接口", produces = "application/json")
    @PostMapping("")
    public ApiResponse messagePush(@Validated @RequestBody MessagePushRequestDTO messagePushRequestDto,
                                   HttpServletRequest request) {
        ObjectUtil.defaultFieldValue(messagePushRequestDto, "platform", !StringUtils.hasText(messagePushRequestDto.getPlatform()),
                MessagePushConstants.PUSH_PLATFORM.ALL.toString());
        return messagePushService.messageHandle(messagePushRequestDto, request);
    }
}
