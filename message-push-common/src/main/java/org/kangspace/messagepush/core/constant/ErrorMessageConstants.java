package org.kangspace.messagepush.core.constant;

/**
 * 错误消息常量类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/27
 */
public interface ErrorMessageConstants {
    /**
     * 协议错误
     */
    String INVALID_PROTOCOL_TYPE_MSG = "错误的请求协议,要求协议为:%s,实际为:%s";
    /**
     * 请求参数错误
     */
    String INVALID_REQUEST_PARAM_MSG = "错误的请求参数,要求为:%s,实际为:%s";
}
