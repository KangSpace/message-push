package org.kangspace.messagepush.core.util;

import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Md5相关工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/2
 */
@Slf4j
public class MD5Util {

    /**
     * 获取列表Hash摘要字符串
     * 摘要逻辑: 1. 输入list元素按Hash排序
     * 2. 转换为,分割的字符串
     * 3. 对字符串取MD5
     *
     * @return
     */
    public static String hashDigest(Collection<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        String hashSort = list.stream().sorted(Comparator.comparingInt(String::hashCode))
                .collect(Collectors.joining(","));
        return MD5.create().digestHex(hashSort);
    }
}
