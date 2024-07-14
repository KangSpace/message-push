package org.kangspace.messagepush.rest.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * uid编码解码器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/19
 */
@Slf4j
public class UidCoder {
    /**
     * <pre>
     * 解码UID
     * figui混淆加密解密算法
     * </pre>
     *
     * @param uidStr 已加密的uid
     * @return 解密的uid
     */
    public static String decode(String uidStr) {
        if (uidStr == null || uidStr.trim().length() == 0) {
            return null;
        }
        uidStr = uidStr.replace("A", "0")
                .replaceAll("(\\D)", "");
        return StringUtils.reverse(uidStr);
    }

    /**
     * 将加密的uid转换为解密的Long
     *
     * @param uidStr 加密的uid
     * @return 解密的uid
     */
    public static Long decodeToLong(String uidStr) {
        Long uidLong = null;
        String uid = decode(uidStr);
        if (uid != null && uid.length() > 0) {
            uidLong = Long.valueOf(uid);
        }
        return uidLong;
    }


    public static void main(String[] args) {
        System.out.println("123 reverse => " + StringUtils.reverse("123"));
        String uidStr = "AwSjG569syGq26A2";
        System.out.println(uidStr.replaceAll("(\\D)", ""));
        System.out.println(uidStr + " => " + decode(uidStr) + " src:20629650");
        uidStr = null;
        System.out.println(uidStr + " => " + decode(uidStr) + " src:null");
        // 38437120
        uidStr = "AaCISHQ2nY173483";
        System.out.println(uidStr + " => " + decode(uidStr) + " src:38437120");
        // '334552042'
        uidStr = "MUqu24oWA255O433";
        System.out.println(uidStr + " => " + decode(uidStr) + " src:334552042");
        // 234234232
        uidStr = "b23SN2D4i32L4i32";
        System.out.println(uidStr + " => " + decode(uidStr) + " src:234234232");

        Long uid = decodeToLong(uidStr);
        System.out.println(uidStr + " => " + uid + " src:234234232");

        uidStr = "中国";
        System.out.println(uidStr + " => " + decode(uidStr) + " src:中国");

    }
}
