package org.kangspace.messagepush.core.elasticsearch.response;

import lombok.Data;

/**
 * @author kango2gler@gmail.com
 * @since 2022/5/16
 */
@Data
public class Result {
    /**
     * 返回结果中的took
     */
    private Long took;
    /**
     * 返回结果中的timeout
     */
    private Boolean timeout;
    /**
     * 返回结果中 hits.total.value
     */
    private Long total;

    /**
     * 响应码
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String error;

    public Result() {
    }

    public Result(Integer status, String error) {
        this.status = status;
        this.error = error;
    }

    public Result(Long took, Boolean timeout, Long total) {
        this.took = took;
        this.timeout = timeout;
        this.total = total;
    }
}
