package org.kangspace.messagepush.consumer.core.domain.dto.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kangspace.messagepush.core.dto.page.PageRequestDto;
import org.kangspace.messagepush.core.elasticsearch.annotation.QueryField;

import java.io.Serializable;

/**
 * 消息推送数据ElasticSearch Query对象
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
@Getter
@Setter
@NoArgsConstructor
@ApiModel("消息推送数据ElasticSearch Query对象")
public class MessagePushRequestDto extends PageRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("messageId")
    @QueryField(field = "message_id")
    private String messageId;

    private String emptyToNull(String str) {
        return str == null || "".equals(str) || str.trim().length() == 0 ? null : str;
    }
}