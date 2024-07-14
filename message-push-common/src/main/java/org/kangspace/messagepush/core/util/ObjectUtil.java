package org.kangspace.messagepush.core.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import javax.el.MethodNotFoundException;
import java.lang.reflect.Method;

/**
 * 对象工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
public class ObjectUtil {
    /**
     * 对象字段设置默认值
     *
     * @param obj          对象
     * @param field        字段名
     * @param setDefault   设置默认的条件
     * @param defaultValue 默认值
     */
    public static void defaultFieldValue(Object obj, String field, boolean setDefault, Object defaultValue) {
        if (obj != null && StringUtils.isNotBlank(field)) {
            Class clazz = obj.getClass();
            if (setDefault) {
                Method setMethod = ReflectionUtils.findMethod(clazz, "set" + field);
                if (setMethod == null) {
                    throw new MethodNotFoundException("class:[" + clazz.getName() + "] field:[" + field + "] method:[set]");
                }
                try {
                    setMethod.setAccessible(true);
                    setMethod.invoke(obj, defaultValue);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }
}
