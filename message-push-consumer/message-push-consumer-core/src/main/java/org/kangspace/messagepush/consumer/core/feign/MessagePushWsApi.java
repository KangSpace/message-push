package org.kangspace.messagepush.consumer.core.feign;


import org.kangspace.messagepush.consumer.core.feign.config.MessagePushWsApiFeignConfig;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 消息websocket推送服务
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@FeignClient(value = "message-push-ws-microservice", path = "/",
        url = "dynamicUrl",
        configuration = MessagePushWsApiFeignConfig.class,
        fallback = MessagePushWsApiFeignFallBack.class)
public interface MessagePushWsApi {
    /**
     * 消息推送PUSH接口
     *
     * @param messagePushRequestDto
     * @return ApiResponse
     */
    @PostMapping("/v1/inner/push")
    ApiResponse messagePush(@RequestBody MessagePushRequestTimeDTO messagePushRequestDto,
                            @RequestHeader(MessagePushWsApiFeignConfig.DEFAULT_SERVICE_NODE_HEADER_KEY) String serviceNode);
}
