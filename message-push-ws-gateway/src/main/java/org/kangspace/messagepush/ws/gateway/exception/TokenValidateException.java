package org.kangspace.messagepush.ws.gateway.exception;

import lombok.Data;

/**
 * @author kango2gler@gmail.com
 * @since 2021/11/5
 */
@Data
public class TokenValidateException extends RuntimeException {
    private ExceptionType exceptionType;

    public TokenValidateException() {
    }

    public TokenValidateException(ExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public TokenValidateException(ExceptionType exceptionType, String message, Throwable cause) {
        super(message, cause);
        this.exceptionType = exceptionType;
    }

    /**
     * 异常类型
     */
    public enum ExceptionType {
        /**
         * 参数错误
         */
        INVALID_PARAM,
        /**
         * token不存在
         */
        ACCESS_TOKEN_NOT_FOUND,
        /**
         * 获取用户信息错误
         */
        SERVER_ERROR
    }
}
