package org.kangspace.messagepush.rest.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

/**
 * 消息推送请求DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@ApiModel("消息推送请求DTO(含时间)")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class MessagePushRequestTimeDTO extends MessagePushRequestDTO {
    /**
     * 消息ID,32位UUID
     */
    @ApiModelProperty("消息ID")
    @JsonProperty("message_id")
    @NotNull(message = "message_id:不能为空")
    private String messageId;
    /**
     * 消息ID,32位UUID
     */
    @ApiModelProperty("消息ID")
    @JsonProperty("app_key")
    @NotNull(message = "app_key:不能为空")
    private String appKey;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonProperty("c_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @NotNull(message = "c_time:不能为空")
    private Date cTime;


    /**
     * 通过{@link MessagePushRequestDTO}创建{@link MessagePushRequestTimeDTO}
     *
     * @param dto MessagePushRequestDto 对象
     * @return MessagePushRequestTimeDto 对象
     */
    public static MessagePushRequestTimeDTO build(MessagePushRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        MessagePushRequestTimeDTO requestTimeDto = new MessagePushRequestTimeDTO();
        requestTimeDto.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        requestTimeDto.setCTime(new Date());
        BeanUtils.copyProperties(dto, requestTimeDto);
        return requestTimeDto;
    }
}
