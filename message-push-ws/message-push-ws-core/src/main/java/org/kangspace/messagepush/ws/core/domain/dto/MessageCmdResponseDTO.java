package org.kangspace.messagepush.ws.core.domain.dto;

import lombok.Data;
import lombok.ToString;
import org.kangspace.messagepush.ws.core.constant.MessagePushCmdEnum;
import org.kangspace.messagepush.ws.core.constant.MessagePushResponseTypeEnum;

/**
 * 消息推送命令DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
@ToString(callSuper = true)
public class MessageCmdResponseDTO extends MessageCmdDTO {
    /**
     * 消息内容类型,取自
     *
     * @see MessagePushResponseTypeEnum
     */
    private String type;

    public MessageCmdResponseDTO() {
        this(null, null);
    }

    public MessageCmdResponseDTO(String type, Object data) {
        super();
        setCmd(MessagePushCmdEnum.RESPONSE.toString());
        setType(type);
        setData(data);
    }
}
