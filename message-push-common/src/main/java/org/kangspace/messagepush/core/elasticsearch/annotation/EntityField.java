package org.kangspace.messagepush.core.elasticsearch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * es属性
 *
 * @author kango2gler@gmail.com
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityField {

    /**
     * es字段名
     *
     * @return java.lang.String
     */
    String field() default "";

}
