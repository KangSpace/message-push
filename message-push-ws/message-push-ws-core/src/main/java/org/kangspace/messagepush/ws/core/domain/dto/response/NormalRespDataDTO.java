package org.kangspace.messagepush.ws.core.domain.dto.response;

import lombok.Data;

/**
 * 响应消息 HEARTBEATl类型 Data内容DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
public class NormalRespDataDTO {
    /**
     * 登录结果: 值为: 1:成功, 0:失败
     */
    private Integer code;
    /**
     * 登录结果描述
     */
    private String msg;

    public NormalRespDataDTO() {
    }

    public NormalRespDataDTO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 默认成功对象
     *
     * @return NormalRespDataDTO
     */
    public static NormalRespDataDTO success() {
        return new NormalRespDataDTO(1, "SUCCESS");
    }

    /**
     * 默认失败对象
     *
     * @return NormalRespDataDTO
     */
    public static NormalRespDataDTO fail() {
        return new NormalRespDataDTO(0, "fail");
    }

    /**
     * 默认失败对象
     *
     * @return NormalRespDataDTO
     */
    public static NormalRespDataDTO fail(String msg) {
        return new NormalRespDataDTO(0, msg);
    }
}
