package org.kangspace.messagepush.ws.core.websocket;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.ws.core.constant.MessagePushCmdEnum;
import org.kangspace.messagepush.ws.core.constant.MessagePushResponseTypeEnum;
import org.kangspace.messagepush.ws.core.constant.MessagePushWsConstants;
import org.kangspace.messagepush.ws.core.domain.dto.MessageCmdDTO;
import org.kangspace.messagepush.ws.core.domain.dto.MessageCmdResponseDTO;
import org.kangspace.messagepush.ws.core.domain.dto.request.LoginReqDataDTO;
import org.kangspace.messagepush.ws.core.domain.dto.response.HeartBeatRespDataDTO;
import org.kangspace.messagepush.ws.core.domain.dto.response.NormalRespDataDTO;
import org.kangspace.messagepush.ws.core.domain.model.MessageRequestParam;
import org.kangspace.messagepush.ws.core.utils.MessagePushHandlerUtils;
import org.kangspace.messagepush.ws.core.websocket.session.SessionHolder;
import org.kangspace.messagepush.ws.core.websocket.session.WebSocketSessionManager;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 消息推送WebsocketHandler
 *
 * @author kango2gler@gmail.com
 * @see WebSocketHandler
 * @since 2021/10/28
 */
@Slf4j
@Data
public class MessagePushWebSocketHandler extends BaseWebcosketHandler implements WebSocketHandler {

    /**
     * 支持的消息类型
     */
    private final List<WebSocketMessage.Type> SUPPORT_MESSAGE_TYPES = Arrays.asList(
            WebSocketMessage.Type.PING,
            WebSocketMessage.Type.TEXT);
    /**
     * session 管理器
     */
    private final WebSocketSessionManager webSocketSessionManager;
    /**
     * 监听路径
     */
    private String endpointPath = MessagePushWsConstants.MESSAGE_V1_ENDPOINT_PATH;
    /**
     * session检查executor
     */
    private ScheduledThreadPoolExecutor sessionCheckExecutor;


    public MessagePushWebSocketHandler(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
        this.sessionCheckExecutor = new ScheduledThreadPoolExecutor(MessagePushWsConstants.TASK_EXECUTOR_THREAD_NUM);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // 1. 获取握手信息
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        log.info("Websocket处理器: 建立连接, session:[{}], handshakeInfo:[{}]", session.getId(), handshakeInfo);
        MessageRequestParam requestParam = MessagePushHandlerUtils.getMessageRequestParam(session);
        // 2. 校验请求参数
        CloseStatus isValid = valid(requestParam, session);
        if (isValid != null) {
            return session.close(isValid);
        }
        String uid = requestParam.getUid();
        // 3. 消息处理
        return session.receive().doOnSubscribe(s -> sessionCheckSchedule(session))
                .doOnTerminate(() -> unRegisterUserSession(uid, session, "Terminate", "连接已断开"))
                .doOnCancel(() -> unRegisterUserSession(uid, session, "Cancel", "连接已取消"))
                .doOnComplete(() -> {
                })
                .doOnNext(message -> {
                    String plainMessage = message.getPayloadAsText();
                    log.info("Websocket处理器: 收到消息, url:[{}], uid:[{}], session:[{}],message:[{}]", endpointPath, uid, session, plainMessage);
                    MessageCmdDTO messageCmdDTO = null;
                    try {
                        messageCmdDTO = JsonUtil.toObject(plainMessage, MessageCmdDTO.class);
                    } catch (Exception e) {
                        log.error("Websocket处理器: 收到消息, 消息反序列化失败,url:[{}], uid:[{}], session:[{}],message:[{}], error:[{}]", endpointPath, uid, session, message, e.getMessage(), e);
                        session.send(error(session, "消息格式错误,请确认JSON格式是否正确,并检查参数内容和参数值。")).toProcessor();
                        return;
                    }
                    if (!StringUtils.hasText(messageCmdDTO.getCmd())) {
                        log.error("Websocket处理器: 收到消息, 消息格式错误,cmd字段值不存在,url:[{}], uid:[{}], session:[{}],message:[{}]", endpointPath, uid, session, message);
                        session.send(error(session, "消息格式错误,cmd字段不能为空")).toProcessor();
                    }
                    messageHandle(message, messageCmdDTO, session, uid);
                }).doOnError((e) -> unRegisterUserSession(uid, session, "Error", "连接发生错误")).then();
    }

    /**
     * 验证请求参数
     *
     * @param requestParam
     * @return boolean
     */
    private CloseStatus valid(MessageRequestParam requestParam, WebSocketSession session) {
        log.info("Websocket处理器: 参数验证, session:[{}], requestParam:[{}]", session, requestParam);
        if (!StringUtils.hasText(requestParam.getUid())) {
            Exception e = new ServerWebInputException("Invalid " + MessagePushConstants.HTTP_HEADER_UID_KEY + " header: value must be not null!");
            log.error("Websocket处理器: 参数验证失败,session:[{}], 错误信息:[{}]", session.getId(), e.getMessage(), e);
            return new CloseStatus(CloseStatus.POLICY_VIOLATION.getCode(), e.getMessage());
        }
        return null;
    }

    /**
     * 注册用户session信息
     *
     * @param uid     用户uid
     * @param session {@link WebSocketSession}
     * @return uid
     */
    private String registerUserSession(WebSocketSession session, String uid, LoginReqDataDTO loginReqData) {
        log.info("Websocket处理器: 注册用户Session信息, url:[{}], uid:[{}], session:[{}], loginReqData:[{}] ", endpointPath, uid, session.getId(), loginReqData);
        webSocketSessionManager.addSession(loginReqData.getAppKey(), uid, new SessionHolder(session, uid, loginReqData.getPlatform()));
        return uid;
    }

    /**
     * 删除用户Session注册信息
     *
     * @param key     uid
     * @param session {@link WebSocketSession}
     * @param reason  删除原因(连接断开,连接取消等)
     */
    private void unRegisterUserSession(String key, WebSocketSession session, String code, String reason) {
        log.info("Websocket处理器, 删除用户Session信息,url[{}], uid:[{}], session:[{}], {} {}", endpointPath, key, session.getId(), code, reason);
        webSocketSessionManager.removeSession(key, session);
    }

    /**
     * 消息处理
     *
     * @param message 消息内容
     * @param session session
     */
    public void messageHandle(WebSocketMessage message, MessageCmdDTO messageCmd, WebSocketSession session, String uid) {
        log.info("Websocket处理器: 消息处理开始, url[{}], uid:[{}], session:[{}], message:[{}], ", endpointPath, uid, session.getId(), message.getPayloadAsText());
        WebSocketMessage.Type messageType = message.getType();
        boolean isSupportedMessageType = supportMessageTypes(message);
        if (!isSupportedMessageType) {
            log.warn("Websocket处理器: 消息处理结束, 不支持的消息类型,忽略处理。 url[{}], uid:[{}], session:[{}], message:[{}], ", endpointPath, uid, session.getId(), message.getPayloadAsText());
            return;
        }
        switch (messageType) {
            case PING:
                // 心跳
                heartbeat(session, true);
                break;
            case TEXT:
                // 登录
                String cmd = messageCmd.getCmd().toUpperCase();
                if (MessagePushCmdEnum.LOGIN.toString().equals(cmd)) {
                    login(session, uid, cmd, messageCmd);
                } else if (MessagePushCmdEnum.HEARTBEAT.toString().equals(cmd)) {
                    heartbeat(session, false);
                }
                break;
            default:
                break;
        }
        log.info("Websocket处理器: 消息处理结束, url[{}], uid:[{}], session:[{}]", endpointPath, uid, session.getId());
    }

    /**
     * 心跳响应
     *
     * @param session WebSocketSession
     */
    private void heartbeat(WebSocketSession session, boolean isPong) {
        String pongMessage = JsonUtil.toFormatJson(new MessageCmdResponseDTO(MessagePushResponseTypeEnum.HEARTBEAT.toString(),
                new HeartBeatRespDataDTO(System.currentTimeMillis())));
        Flux<WebSocketMessage> pong;
        if (isPong) {
            pong = Flux.just(session.pongMessage((dbf) -> dbf.wrap(pongMessage.getBytes())));
        } else {
            pong = Flux.just(session.textMessage(pongMessage));
        }
        session.send(pong).toProcessor();
    }

    /**
     * 登录处理
     *
     * @param session    session
     * @param uid        用户ID
     * @param cmd        命令
     * @param messageCmd 命令对象
     */
    private void login(WebSocketSession session, String uid, String cmd, MessageCmdDTO messageCmd) {
        // 验证登录信息字段
        String loginInfoStr = JsonUtil.toFormatJson(messageCmd.getData());
        LoginReqDataDTO loginReqData = JsonUtil.toObject(loginInfoStr, LoginReqDataDTO.class);
        if (!StringUtils.hasText(loginReqData.getAppKey()) ||
                !StringUtils.hasText(loginReqData.getPlatform())) {
            MessageCmdResponseDTO cmdDTO = new MessageCmdResponseDTO(cmd,
                    new NormalRespDataDTO(MessagePushConstants.FAIL_CODE,
                            "登录接口参数错误,请检查输入参数,必填参数项不能为空"));
            session.send(WebsocketUtils.textMessage(session, JsonUtil.toFormatJson(cmdDTO))).toProcessor();
            return;
        }
        if (sessionCheck(session)) {
            log.warn("Websocket处理器: 当前Session已登录,不可重复登录, url[{}], uid:[{}], session:[{}]", endpointPath, uid, session.getId());
            session.send(WebsocketUtils.textMessage(session, new MessageCmdResponseDTO(cmd, NormalRespDataDTO.fail("当前连接已登录,请勿多次登录")))).toProcessor();
            return;
        }
        // 注册用户session信息
        registerUserSession(session, uid, loginReqData);
        session.send(WebsocketUtils.textMessage(session, new MessageCmdResponseDTO(cmd, NormalRespDataDTO.success()))).toProcessor();
        log.info("Websocket处理器: 登录成功, url[{}], uid:[{}], session:[{}]", endpointPath, uid, session.getId());
    }

    /**
     * <pre>
     * Session 检查
     * 检查规定时间内的连接是否已登录,若未登录则断开连接
     * </pre>
     *
     * @param session session
     */
    private void sessionCheckSchedule(WebSocketSession session) {
        // 登录超时处理
        sessionCheckExecutor.schedule(() -> {
            boolean isLogin = sessionCheck(session);
            if (!isLogin) {
                session.send(WebsocketUtils.textMessage(session, new MessageCmdResponseDTO(MessagePushResponseTypeEnum.ERROR.getVal(),
                        NormalRespDataDTO.fail("login timeout!")))).toProcessor();
                session.close(CloseStatus.POLICY_VIOLATION.withReason("login timeout!")).toProcessor();
                log.info("Websocket处理器: Session长时间未登录自动断开,session:[{}]", session.getId());
            }
        }, MessagePushWsConstants.SESSION_CHECK_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 检查当前session是否已登录
     *
     * @param session WebSocketSession
     * @return true:已登录,false: 未登录
     */
    private boolean sessionCheck(WebSocketSession session) {
        return webSocketSessionManager.sessionCheck(session);
    }

    /**
     * 是否支持当前消息类型
     *
     * @param message WebSocketMessage
     * @return boolean
     */
    public boolean supportMessageTypes(WebSocketMessage message) {
        return SUPPORT_MESSAGE_TYPES.contains(message.getType());
    }

}
