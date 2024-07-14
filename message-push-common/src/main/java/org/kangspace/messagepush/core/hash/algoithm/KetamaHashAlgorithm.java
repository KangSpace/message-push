package org.kangspace.messagepush.core.hash.algoithm;

/**
 * Ketama key Hash算法(32位)
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/22
 */
public class KetamaHashAlgorithm implements HashAlgorithm {
    /**
     * Ketama key hash 算法
     *
     * @param node 待hash字符串
     * @return hash值
     */
    @Override
    public Long hashing(String node) {
        byte[] digest = md5(node);
        return hash(digest, 0);
    }


    @Override
    public Long hashing(byte[] digest, int number) {
        return hash(digest, number);
    }


    /**
     * Ketama Hash
     *
     * @param digest MD5 digest
     * @param number 序号
     * @return hash
     */
    private Long hash(byte[] digest, int number) {
        return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                | (digest[number * 4] & 0xFF))
                & 0xFFFFFFFFL;
    }


}
