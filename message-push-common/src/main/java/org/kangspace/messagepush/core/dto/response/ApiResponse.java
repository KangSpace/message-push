package org.kangspace.messagepush.core.dto.response;

import org.kangspace.messagepush.core.enums.ResponseEnum;

/**
 * @author kango2gler@gmail.com
 * @date 2024/7/13
 * @since
 */
public class ApiResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public ApiResponse() {
        this.code = ResponseEnum.OK.getValue();
    }

    public ApiResponse(T data) {
        this.code = ResponseEnum.OK.getValue();
        this.data = data;
    }

    public ApiResponse(ResponseEnum responseEnum) {
        this.code = responseEnum.getValue();
        this.msg = responseEnum.getReasonPhrase();
        this.data = null;
    }

    public ApiResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }

    public ApiResponse(final Integer code, final String msg, final T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(final Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(final String msg) {
        this.msg = msg;
    }

    public T getData() {
        return this.data;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public String toString() {
        return "ApiResponse(code=" + this.getCode() + ", msg=" + this.getMsg() + ", data=" + this.getData() + ")";
    }
}
