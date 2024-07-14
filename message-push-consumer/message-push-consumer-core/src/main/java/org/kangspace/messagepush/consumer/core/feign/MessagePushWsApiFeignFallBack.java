package org.kangspace.messagepush.consumer.core.feign;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.springframework.stereotype.Service;

/**
 * 默认feign降级处理
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@Slf4j
@Service
public class MessagePushWsApiFeignFallBack implements MessagePushWsApi {

    @Override
    public ApiResponse messagePush(MessagePushRequestTimeDTO messagePushRequestDto, String serviceNode) {
        log.error("messagePush接口访问失败,进入FallBack. args: serviceNode:[{}],dto: [{}]", serviceNode, messagePushRequestDto);
        return null;
    }
}
