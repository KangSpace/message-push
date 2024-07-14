package org.kangspace.messagepush.rest.core.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.MD5;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * <pre>
 * 应用信息生成器
 * appId: 32位UUID
 * appSecret: md5("messagepush"+{appId})
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
public class AppGenerator {
    /**
     * AppSecret生成的加密因子
     */
    private static final byte[] APP_SECRET_SEEDS = "messagepush".getBytes(StandardCharsets.UTF_8);

    /**
     * 生成应用信息
     *
     * @return {@link AppInfo}
     */
    public static AppInfo generate() {
        String appKey = UUID.fastUUID().toString(true);
        String appSecret = generateAppSecret(appKey);
        return new AppInfo(appKey, appSecret);
    }

    /**
     * 通过AppKey生成AppSecret
     *
     * @param appKey appKey
     * @return AppSecret
     */
    private static String generateAppSecret(String appKey) {
        return new MD5(APP_SECRET_SEEDS).digestHex16(appKey);
    }

    /**
     * 验证应用信息
     *
     * @param appKey    appKey
     * @param appSecret appSecret
     * @return boolean
     */
    public static boolean validAppInfo(String appKey, String appSecret) {
        if (StringUtils.hasText(appKey) && StringUtils.hasText(appSecret)) {
            String correctSecret = generateAppSecret(appKey);
            return correctSecret.equals(appSecret);
        }
        return false;
    }

    public static void main(String[] args) {
        AppInfo appInfo = AppGenerator.generate();
        System.out.println(appInfo);
    }

    /**
     * Http Basic认证头
     */
    @Data
    public static class AppInfo {
        private String appKey;
        private String appSecret;

        public AppInfo() {
        }

        public AppInfo(String appKey, String appSecret) {
            this.appKey = appKey;
            this.appSecret = appSecret;
        }
    }
}
