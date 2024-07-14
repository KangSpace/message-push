package org.kangspace.messagepush.ws.gateway.constant;

/**
 * 常量类
 *
 * @author kango2gler@gmail.com
 * @date 2021/10/28
 */
public interface MessagePushWsConstants {

    /**
     * 模拟UID请求头
     * 用于压测情况;(正常业务情况下建立连接需要传认证token,考虑到压测时无法模拟大批量token)
     */
    String MOCK_UID_HEADER = "x-mock-uid";

}
