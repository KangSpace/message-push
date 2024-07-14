package org.kangspace.messagepush.ws.core.constant;

/**
 * 命令枚举
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
public enum MessagePushResponseTypeEnum {
    /**
     * 登录
     */
    LOGIN,
    /**
     * 心跳
     */
    HEARTBEAT,
    /**
     * 消息响应
     */
    MESSAGE,
    /**
     * 错误
     */
    ERROR,
    ;

    public String getVal() {
        return toString();
    }
}
