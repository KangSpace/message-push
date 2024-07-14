package org.kangspace.messagepush.ws.core.constant;

/**
 * 命令枚举
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
public enum MessagePushCmdEnum {
    /**
     * 登录
     */
    LOGIN,
    /**
     * 心跳
     */
    HEARTBEAT,
    /**
     * 响应
     */
    RESPONSE,

    ;

    public String getVal() {
        return toString();
    }
}
