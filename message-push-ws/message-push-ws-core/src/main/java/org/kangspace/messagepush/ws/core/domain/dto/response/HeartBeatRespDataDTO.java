package org.kangspace.messagepush.ws.core.domain.dto.response;

import lombok.Data;

/**
 * 响应消息 HEARTBEATl类型 Data内容DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
public class HeartBeatRespDataDTO {
    /**
     * 服务端当前时间毫秒数
     */
    private Long t;

    public HeartBeatRespDataDTO() {
    }

    public HeartBeatRespDataDTO(Long t) {
        this.t = t;
    }
}
