package org.kangspace.messagepush.ws.core.websocket;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.util.JsonUtil;
import org.kangspace.messagepush.ws.core.constant.MessagePushResponseTypeEnum;
import org.kangspace.messagepush.ws.core.domain.dto.MessageCmdResponseDTO;
import org.kangspace.messagepush.ws.core.domain.dto.response.MessageRespDataDTO;
import org.kangspace.messagepush.ws.core.domain.model.HttpPushMessageDTO;
import org.kangspace.messagepush.ws.core.websocket.session.SessionHolder;
import org.kangspace.messagepush.ws.core.websocket.session.WebSocketSessionManager;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Http消息消费者
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/30
 */
@Slf4j
public class HttpPushMessageConsumer<T extends HttpPushMessageDTO> implements Consumer<T> {
    private final WebSocketSessionManager webSocketSessionManager;

    public HttpPushMessageConsumer(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
    }


    /**
     * 消息消费
     *
     * @param messageDto 消息内容
     */
    @Override
    public void accept(HttpPushMessageDTO messageDto) {
        log.info("Http推送消息消费: 消费开始, message:[{}]", messageDto);
        try {
            consume(messageDto);
        } catch (Exception e) {
            log.error("Http推送消息消费: 异常, error:{}", e.getMessage(), e);
        }


    }

    public void consume(HttpPushMessageDTO messageDto) {
        String appKey = messageDto.getAppKey();
        String platform = messageDto.getPlatform();
        String message = JsonUtil.toFormatJson(messageDto.getMessage());
        List<String> audiences = messageDto.getAudience().getUids();
        if (CollectionUtils.isEmpty(audiences)) {
            log.info("Http推送消息消费: 消费结束, 无目标用户IDs,message:[{}]", messageDto);
            return;
        }
        Map<String, List<SessionHolder>> sessionHolders = webSocketSessionManager.getKeySessions(appKey);
        Set<String> keys = sessionHolders.keySet();
        List<String> existsAlias = audiences.stream().filter(keys::contains).collect(Collectors.toList());
        AtomicInteger pushCount = new AtomicInteger(0);
        // 数据发送
        existsAlias.forEach(alias -> {
            sessionHolders.get(alias).stream()
                    .filter(sessionHolder -> sessionHolder.getPlatform() == null || matchPlatform(sessionHolder.getPlatform(), platform))
                    .forEach(sessionHolder -> {
                        WebSocketSession session = sessionHolder.getSession();
                        // 推送消息到客户端
                        MessageCmdResponseDTO responseMessage = new MessageCmdResponseDTO(MessagePushResponseTypeEnum.MESSAGE.toString(),
                                JsonUtil.toObject(message, MessageRespDataDTO.class));
                        try {
                            WebsocketUtils.sendTextMessage(session, responseMessage);
                            pushCount.addAndGet(1);
                        } catch (Exception e) {
                            log.error("Http推送消息消费: 消费错误, uid:[{}], platform:[{}], message:[{}]", sessionHolder.getUid(),
                                    sessionHolder.getPlatform(), responseMessage);
                        }
                    });
        });
        log.info("Http推送消息消费: 消费结束,消息中的别名数量:{},消息中的目标平台:{},当前服务维持的匹配用户数:[{}] 推送数量:[{}] ",
                audiences.size(), platform, existsAlias.size(), pushCount.get());
    }

    /**
     * 校验目标平台是否匹配
     *
     * @param sessionPlatform       sessionPlatform
     * @param messageTargetPlatform messageTargetPlatform
     * @return boolean
     */
    private boolean matchPlatform(String sessionPlatform, String messageTargetPlatform) {
        String target = messageTargetPlatform.toLowerCase();
        return "".equals(target) || MessagePushConstants.PUSH_PLATFORM.ALL.toString().equalsIgnoreCase(target) ||
                target.equals(sessionPlatform);
    }
}
