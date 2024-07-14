package org.kangspace.messagepush.rest.core.constant;

/**
 * 常量类
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
public interface MessagePushConstants {
    /**
     * 推送目标平台
     */
    enum PUSH_PLATFORM {
        ALL,
        H5,
        Android,
        iOS;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
