package org.kangspace.messagepush.ws.gateway.filter.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * 网关Websocket双向session持有对象
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionProxyHolder {
    /**
     * 后端服务Session
     */
    private WebSocketSession proxySession;
    /**
     * 当前服务Session
     */
    private WebSocketSession session;
    /**
     * 服务节点,值为: HOST:IP
     */
    private String serviceNode;
}
