package org.kangspace.messagepush.core.elasticsearch.annotation;


import org.kangspace.messagepush.core.elasticsearch.enumeration.OccurEnum;
import org.kangspace.messagepush.core.elasticsearch.enumeration.ParseEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * es查询对象辅助注解
 *
 * @author kango2gler@gmail.com
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {
    /**
     * 查询处理注解
     *
     * @return ParseEnum
     */
    ParseEnum value() default ParseEnum.TERM;

    /**
     * es字段名
     *
     * @return java.lang.String
     */
    String field() default "";

    /**
     * 在must、filter、should中哪个位置中块拼装
     *
     * @return OccurEnum
     */
    OccurEnum occur() default OccurEnum.FILTER;

    /**
     * 格式化表达式，主要用于日期格式，QueryUtil中默认格式：yyyy-MM-dd'T'HH:mm:ss
     *
     * @return java.lang.String
     */
    String format() default "";


}
