package org.kangspace.messagepush.core.hash.algoithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * key Hash算法(32位)
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/22
 */
public interface HashAlgorithm {
    /**
     * key hash 算法
     *
     * @param node 待hash字符串
     * @return hash
     */
    Long hashing(String node);

    /**
     * Ketama key hash 算法
     *
     * @param digest md5后的byte[]
     * @param number 虚拟节点索引
     * @return hash
     */
    Long hashing(byte[] digest, int number);

    /**
     * MD5
     *
     * @param str 待计算的字符串
     * @return byte[]
     */
    default byte[] md5(String str) {
        String algorithm = "MD5";
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return md.digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return str.getBytes();
        }

    }
}
