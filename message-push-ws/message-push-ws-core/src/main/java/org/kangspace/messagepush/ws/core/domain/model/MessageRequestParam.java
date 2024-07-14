package org.kangspace.messagepush.ws.core.domain.model;

import lombok.Data;

/**
 * 消息推送请求参数
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
@Data
public class MessageRequestParam {
    /**
     * 用户Token
     * 从请求头 Authorization Bearer Token中获取
     */
    private String token;
    /**
     * 用户uid
     * 从请求头或 用户token中获取
     */
    private String uid;

    public MessageRequestParam() {
    }

    public MessageRequestParam(String token, String uid) {
        this.token = token;
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "token='" + token + '\'' + ", uid='" + uid + '\'';
    }
}
