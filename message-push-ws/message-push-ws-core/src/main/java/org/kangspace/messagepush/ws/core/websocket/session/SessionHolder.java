package org.kangspace.messagepush.ws.core.websocket.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * Session管理器中保存实体
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionHolder {
    /**
     * session
     */
    private WebSocketSession session;
    /**
     * 用户ID
     */
    private String uid;
    /**
     * 目标平台
     */
    private String platform;

}
