package org.kangspace.messagepush.core.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <pre>
 * 路径参数解析器:
 * 将路径中的花括号包裹的变量替换为实际值,如:
 * /user/{id}/info 替换为/user/1/info
 * /user/type/{type}/ 替换为 /user/type/1
 * 其中 id=1,type=1。
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/10
 */
public class PathVariableResolver {
    /**
     * 路径参数正则
     */
    public static Pattern PATH_VARIABLES_PATTERN = Pattern.compile("(\\{.*?\\})", Pattern.MULTILINE);
    /**
     * 待处理的url
     */
    private String url;
    /**
     * 参数map
     */
    private Map<String, String> variableMap = new HashMap<>(1);

    private PathVariableResolver() {
    }

    public PathVariableResolver(String url, Map<String, String> variableMap) {
        Objects.requireNonNull(url, "url不能为空");
        this.url = url;
        if (variableMap != null) {
            this.variableMap = variableMap;
        }
    }

    /**
     * 将URL中的变量处理为具体值,并返回最终URL
     *
     * @return 替换变量后的URL
     */
    public String resolve() {
        String url = this.url;
        if (!StringUtils.hasText(url)) {
            return url;
        }
        List<String> pathVariables = this.findPathVariables();
        if (CollectionUtils.isEmpty(pathVariables)) {
            return url;
        }
        Map<String, String> variableMap = this.variableMap;
        for (String pv : pathVariables) {
            url = url.replaceAll(toVariableReplacePattern(pv), nullToEmpty(variableMap.get(pv)));
        }
        return url;
    }


    /**
     * 查找所有的路径变量
     *
     * @return all pathVariables
     */
    public List<String> findPathVariables() {
        String url = this.url;
        Matcher matcher = PATH_VARIABLES_PATTERN.matcher(url);
        List<String> pathVariables = new ArrayList<>(4);
        while (matcher.find()) {
            pathVariables.add(removeBraces(matcher.group()));
        }
        return pathVariables.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 移除大括号
     *
     * @param str 待处理的字符串
     * @return 新字符串
     */
    private String removeBraces(String str) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        return str.replace("{", "").replace("}", "");
    }

    /**
     * 转换为变量替换的pattern
     *
     * @param var 待处理的字符串
     * @return 带{}的字符串
     */
    private String toVariableReplacePattern(String var) {
        return "\\{" + var + "\\}";
    }

    private String nullToEmpty(Object str) {
        return str != null ? str.toString() : "";
    }
}
