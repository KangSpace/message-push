package org.kangspace.messagepush.ws.core.constant;

/**
 * 常量类
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
public interface MessagePushWsConstants {
    /**
     * 消息订阅V1路径
     */
    String MESSAGE_V1_ENDPOINT_PATH = "/v1/message";

    /**
     * 任务执行线程数
     */
    int TASK_EXECUTOR_THREAD_NUM = 10;
    /**
     * Session登录检查超时时间
     */
    long SESSION_CHECK_DELAY_SECONDS = 10;
}
