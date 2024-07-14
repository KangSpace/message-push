package org.kangspace.messagepush.rest.core;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kangspace.messagepush.rest.core.utils.VariableUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * 公共测试类型
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
@RunWith(JUnit4.class)
public class Test {
    Pattern levelPattern = Pattern.compile("(?i)^((info)|(warn)|(debug)|(error)|(fatal))$");

    @org.junit.Test
    public void levelPatternTest() {
        List<String> rightLevels = Arrays.asList("info", "warn", "debug", "Error", "FATAL");
        List<String> errorLevels = Arrays.asList("info1", "1warn", "consumer", "123");
        for (String rightLevel : rightLevels) {
            Assert.assertTrue("rightLevel:" + rightLevel + "校验错误!", levelPattern.matcher(rightLevel).find());
        }
        for (String errorLevel : errorLevels) {
            Assert.assertFalse("errorLevel:" + errorLevel + "校验错误!", levelPattern.matcher(errorLevel).find());
        }
    }

    @org.junit.Test
    public void uuid() {
        System.out.println(IdUtil.fastSimpleUUID());
    }


    @org.junit.Test
    public void variableSwapTest() {
        String appName = "APP";
        String logId = "5cafe88ae6f64faeb356155fda0cefad";
        Map<String, String> varReplaceMap = ImmutableMap.of(
                "{{LOG_ID}}", logId,
                "{{APP_NAME}}", appName
        );
        List<String> testStrs = Arrays.asList(
                null,
                "这是应用：{{APP_NAME}}，数据ID为：{{LOG_ID}}",
                "应用：{{APP_NAME}}",
                "数据ID为：{{LOG_ID}}",
                "一个三四五",
                "上山打老虎OK",
                null
        );
        System.out.println("源数据:");
        System.out.println(String.join("\n", testStrs));
        System.out.println("\n 替换后的数据:");
        testStrs.forEach(t -> {
            String newStr = VariableUtils.variableSwap(t, varReplaceMap);
            System.out.println(newStr);
        });
    }

    /**
     * 1000个别名
     */
    @org.junit.Test
    public void alias1000Test() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            sb.append("\"alias" + i + "\",");
        }
        System.out.println(sb.toString());
    }
}
