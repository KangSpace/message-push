package org.kangspace.messagepush.ws.gateway.model;

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
     * 参数来源
     */
    private ParamFrom paramFrom = ParamFrom.NULL;
    /**
     * 用户Token
     * 从请求头 Authorization Bearer Token中获取
     */
    private String token;
    /**
     * 生成passport token所使用的应用ID
     */
    private String tokenAppId;

    /**
     * 通过token获取用户信息中获取uid
     */
    private String uid;

    public MessageRequestParam() {
    }

    public MessageRequestParam(String token, String tokenAppId, ParamFrom paramFrom) {
        this.token = token;
        this.tokenAppId = tokenAppId;
        this.paramFrom = paramFrom;
    }

    /**
     * 参数来源
     */
    public enum ParamFrom {
        /**
         * 参数在URL中
         */
        URL,
        /**
         * 参数在请求头中
         */
        HEADER,
        /**
         * 参数不存在
         */
        NULL
    }
}
