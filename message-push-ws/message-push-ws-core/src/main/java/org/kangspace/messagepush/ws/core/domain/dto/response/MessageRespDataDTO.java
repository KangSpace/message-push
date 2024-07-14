package org.kangspace.messagepush.ws.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 消息命令Data内容DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
public class MessageRespDataDTO {
    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型，由消息发送方自定义内容类型
     */
    @JsonProperty(value = "content_type")
    private String contentType;

    /**
     * 扩展内容，由消息发送方自定义扩展
     */
    private String extras;

}
