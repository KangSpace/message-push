package org.kangspace.messagepush.ws.core.domain.model;

import lombok.Data;
import lombok.ToString;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestTimeDTO;
import org.springframework.beans.BeanUtils;

/**
 * Http推送消息DTO类
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/1
 */
@Data
@ToString(callSuper = true)
public class HttpPushMessageDTO extends MessagePushRequestTimeDTO {

    /**
     * 将messagePushRequestTimeDTO转换为HttpPushMessageDTO
     *
     * @param messagePushRequestTimeDTO {@link MessagePushRequestTimeDTO}
     * @return HttpPushMessageDTO
     */
    public static HttpPushMessageDTO from(MessagePushRequestTimeDTO messagePushRequestTimeDTO) {
        HttpPushMessageDTO result = new HttpPushMessageDTO();
        BeanUtils.copyProperties(messagePushRequestTimeDTO, result);
        return result;
    }
}
