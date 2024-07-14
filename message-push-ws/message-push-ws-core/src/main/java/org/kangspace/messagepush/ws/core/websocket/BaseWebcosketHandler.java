package org.kangspace.messagepush.ws.core.websocket;


import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.ws.core.constant.MessagePushResponseTypeEnum;
import org.kangspace.messagepush.ws.core.domain.dto.MessageCmdResponseDTO;
import org.kangspace.messagepush.ws.core.domain.dto.response.NormalRespDataDTO;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;

/**
 * WebcosketHandler 基类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
public class BaseWebcosketHandler {

    /**
     * 错误处理
     *
     * @param session session
     * @param code    错误码
     * @param msg     错误信息
     * @return Flux<WebSocketMessage>
     */
    public Flux<WebSocketMessage> error(WebSocketSession session, int code, String msg) {
        return WebsocketUtils.textMessage(session, JsonUtil.toFormatJson(new MessageCmdResponseDTO(MessagePushResponseTypeEnum.ERROR.getVal(),
                new NormalRespDataDTO(code, msg))));
    }

    /**
     * 错误处理
     *
     * @param session session
     * @param msg     错误信息
     * @return Flux<WebSocketMessage>
     */
    public Flux<WebSocketMessage> error(WebSocketSession session, String msg) {
        return error(session, MessagePushConstants.FAIL_CODE, msg);
    }

}
