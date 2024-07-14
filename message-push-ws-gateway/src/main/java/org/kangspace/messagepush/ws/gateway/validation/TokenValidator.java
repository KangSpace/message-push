package org.kangspace.messagepush.ws.gateway.validation;

import java.util.function.Consumer;

/**
 * Token验证接口
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/5
 */
public interface TokenValidator<T> {

    /**
     * 验证token,并返回token获取的用户信息
     *
     * @param token    token
     * @param callback token获取成功的回调
     * @return token验证成功获取的用户信息
     */
    T valid(String authAppId, String token, Consumer<T> callback);
}
