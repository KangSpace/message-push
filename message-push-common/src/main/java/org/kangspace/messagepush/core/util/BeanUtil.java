package org.kangspace.messagepush.core.util;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * bean工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021-04-23
 */
public class BeanUtil {

    /**
     * 设置bean对象中不再fields中的属性为null(兼容大小写和_)
     *
     * @param bean       对象
     * @param keepFields 保留的属性名
     * @param <T>        对象泛型
     * @throws IllegalAccessException
     */
    public static <T> void setFieldsNull(T bean, List<String> keepFields) throws IllegalAccessException {
        if (ListUtil.isEmpty(keepFields)) {
            return;
        }
        //小写的保留字段名
        List<String> lowerKeepFields = keepFields.stream().map(f -> f.toLowerCase()).collect(Collectors.toList());
        //遍历对象属性
        for (Field field : bean.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(bean) == null) {
                continue;
            }
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            //存在JsonProperty 且 value不为空 则 使用jsonProperty.value()作为属性名判断
            if (jsonProperty != null && StrUtil.isNotEmpty(jsonProperty.value())) {
                //fields不包含该属性名则置空该属性
                if (!lowerKeepFields.contains(jsonProperty.value().toLowerCase())) {
                    field.set(bean, null);
                }
                //存在JsonProperty 或 JsonProperty.value不为空 则 使用原始属性名判断，fields不包含该属性名则置空该属性
            } else if (!lowerKeepFields.contains(field.getName().toLowerCase())) {
                field.set(bean, null);
            }
        }
    }

    /**
     * 返回实现的接口或继承的父类的第一个泛型（优先取父类中的泛型）
     *
     * @param o
     * @return
     */
    public static Class getGenericsClass(Object o) {
        List<Type> parentClasses = ListUtil.arrayToList(o.getClass().getGenericInterfaces());
        parentClasses.add(o.getClass().getGenericSuperclass());
        if (o.getClass().getSuperclass() != null) {
            parentClasses.addAll(ListUtil.arrayToList(o.getClass().getSuperclass().getGenericInterfaces()));
        }
        for (Type type : parentClasses) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types == null || types.length == 0) {
                continue;
            }
            if (!(types[0] instanceof ParameterizedType)) {
                return ((Class) types[0]);
            }
            return (Class) ((ParameterizedType) types[0]).getRawType();
        }
        return String.class;
    }

}
