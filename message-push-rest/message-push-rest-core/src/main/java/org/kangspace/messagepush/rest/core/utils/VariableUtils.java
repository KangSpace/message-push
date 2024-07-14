package org.kangspace.messagepush.rest.core.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 变量处理工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/18
 */
public class VariableUtils {
    /**
     * 变量替换
     *
     * @param str           输入字符串
     * @param varReplaceMap 变量-替换值 map(key:变量,value:替换值)
     * @return String
     */
    public static String variableSwap(String str, Map<String, String> varReplaceMap) {
        if (!StringUtils.hasText(str) || CollectionUtils.isEmpty(varReplaceMap)) {
            return str;
        }
        for (String v : varReplaceMap.keySet()) {
            if (str.contains(v)) {
                str = str.replace(v, varReplaceMap.get(v));
            }
        }
        return str;
    }
}
