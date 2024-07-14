package org.kangspace.messagepush.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kangspace.messagepush.core.util.PathVariableResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 路径替换处理器测试类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/10
 */
@RunWith(JUnit4.class)
public class PathVariableResolverTest {

    /**
     * 路径正则测试
     */
    @Test
    public void pathPatternTest() {
        String test = "/user/{id}/{name}/{id}";
        Matcher matcher = PathVariableResolver.PATH_VARIABLES_PATTERN.matcher(test);
        while (matcher.find()) {
            int count = matcher.groupCount();
            System.out.println(matcher.group());
        }
    }

    /**
     * 获取路径参数测试
     */
    @Test
    public void findPathVariablesTest() {
        String test = "/user/{id}/{name}/{id}";
        PathVariableResolver pvr = new PathVariableResolver(test, null);
        System.out.println(pvr.findPathVariables());
    }


    /**
     * 路径替换测试
     */
    @Test
    public void pathVariablesResolveTest() {
        String test = "/user/{id}/{name}/{id}";
        Map<String, String> variableMap = new HashMap<>(2);
        variableMap.put("id", "1");
        variableMap.put("name", "will");
        PathVariableResolver pvr = new PathVariableResolver(test, variableMap);
        System.out.println(pvr.resolve());
    }


}
