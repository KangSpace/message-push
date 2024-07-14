package org.kangspace.messagepush.rest.core.constant;

/**
 * @author kango2gler@gmail.com
 * @since 2021/11/1
 */
public class AppThreadLocal {
    private static ThreadLocal<String> threadLocal = new ThreadLocal();

    private AppThreadLocal() {
    }

    /**
     * 设置AppKey
     *
     * @param appKey appKey
     * @return appKey
     */
    public static String setAppKey(String appKey) {
        threadLocal.set(appKey);
        return appKey;
    }

    /**
     * 获取AppKey
     *
     * @return appKey
     */
    public static String getAppKey() {
        return (String) threadLocal.get();
    }

    /**
     * 重置AppThreadLocal
     */
    public static void reset() {
        threadLocal.remove();
    }
}
