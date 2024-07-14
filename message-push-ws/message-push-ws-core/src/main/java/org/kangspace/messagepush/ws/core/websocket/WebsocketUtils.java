package org.kangspace.messagepush.ws.core.websocket;


import org.kangspace.messagepush.core.util.JsonUtil;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;

/**
 * Websocket相关Util
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/1
 */
public class WebsocketUtils {


    /**
     * 输出文本消息
     *
     * @param session session
     * @param msg     消息内容
     * @return Flux<WebSocketMessage>
     */
    public static Flux<WebSocketMessage> textMessage(WebSocketSession session, Object msg) {
        return textMessage(session, JsonUtil.toFormatJson(msg));
    }

    /**
     * 输出文本消息
     *
     * @param session session
     * @param msg     消息内容
     * @return Flux<WebSocketMessage>
     */
    public static Flux<WebSocketMessage> textMessage(WebSocketSession session, String msg) {
        return Flux.just(session.textMessage(msg));
    }

    /**
     * 输出文本消息
     *
     * @param session session
     * @param msg     消息内容
     */
    public static void sendTextMessage(WebSocketSession session, String msg) {
        session.send(textMessage(session, msg)).toProcessor();
    }

    /**
     * 输出文本消息
     *
     * @param session session
     * @param msg     消息内容
     */
    public static void sendTextMessage(WebSocketSession session, Object msg) {
        session.send(textMessage(session, msg)).toProcessor();
    }

}
