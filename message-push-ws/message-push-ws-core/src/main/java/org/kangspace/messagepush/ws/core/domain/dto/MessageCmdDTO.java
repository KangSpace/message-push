package org.kangspace.messagepush.ws.core.domain.dto;

import lombok.Data;

/**
 * <pre>
 * 消息推送命令DTO基础类
 * (客户端发送命令和服务端影响命令都依赖该类)
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
public class MessageCmdDTO {
    /**
     * 命令
     */
    private String cmd;
    private Object data;

}
