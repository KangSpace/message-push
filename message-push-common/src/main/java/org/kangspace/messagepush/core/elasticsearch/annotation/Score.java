package org.kangspace.messagepush.core.elasticsearch.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ES分数注解
 *
 * @author kango2gler@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Score {
}
