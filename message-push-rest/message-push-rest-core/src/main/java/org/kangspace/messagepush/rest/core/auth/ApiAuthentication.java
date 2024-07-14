package org.kangspace.messagepush.rest.core.auth;

import java.lang.annotation.*;

/**
 * API请求认证注解
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiAuthentication {
}
