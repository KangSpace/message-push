package org.kangspace.messagepush.common;

import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kangspace.messagepush.core.util.MD5Util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Md5相关工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/2
 */
@Slf4j
@RunWith(JUnit4.class)
public class MD5UtilTest {

    /**
     * 获取列表Hash摘要字符串
     * 摘要逻辑: 1. 输入list元素按Hash排序
     * 2. 转换为,分割的字符串
     * 3. 对字符串取MD5
     *
     * @return
     */
    @Test
    public void testHashDigest() {
        List<String> serverList = Arrays.asList(
                "192.168.0.9:8080",
                "192.168.0.6:8080",
                "192.168.0.1:8080",
                "192.168.0.2:8080",
                "192.168.0.4:8080",
                "192.168.0.3:8080",
                "192.168.0.8:8080",
                "192.168.0.7:8080"
        );
        String utilResult = MD5Util.hashDigest(serverList);
        log.info("utilResult:{}", utilResult);
        Assert.assertTrue("验证失败,hash排序生成失败", utilResult != null);
    }

    public String listHash(List<String> list) {
        Set<String> set = list.stream().collect(Collectors.toSet());
        String hashSort = set.stream().collect(Collectors.joining(","));
        String result = MD5.create().digestHex(hashSort);
        log.info("listHash by hash set, list:[{}], hashSort:[{}]", list, hashSort);
        return result;
    }
}
