package org.kangspace.messagepush.ws.core.utils;

import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.util.HttpUtils;
import org.kangspace.messagepush.ws.core.domain.model.MessageRequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
public class MessagePushHandlerUtils {

    /**
     * 获取消息请求参数
     *
     * @param session {@link WebSocketSession}
     * @return {@link MessageRequestParam}
     */
    public static MessageRequestParam getMessageRequestParam(WebSocketSession session) {
        HttpHeaders headers = session.getHandshakeInfo().getHeaders();
        String token = HttpUtils.getHttpBearerToken(headers);
        String uid = headers.getFirst(MessagePushConstants.HTTP_HEADER_UID_KEY);
        return new MessageRequestParam(token, uid);
    }

}
