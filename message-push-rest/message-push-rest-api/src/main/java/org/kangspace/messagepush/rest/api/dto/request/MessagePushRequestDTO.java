package org.kangspace.messagepush.rest.api.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kangspace.messagepush.rest.api.dto.ApiBaseDTO;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 消息推送请求DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@ApiModel("消息推送请求DTO")
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MessagePushRequestDTO extends ApiBaseDTO {
    /**
     * 推送方式；1: websocket方式
     * 必填
     */
    @JsonProperty("push_method")
    @NotNull(message = "push_method:推送方式不能为空")
    @Min(value = 1, message = "push_method:值错误,取值范围: 1:websocket")
    @Max(value = 1, message = "push_method:值错误,取值范围: 1:websocket")
    private Integer pushMethod;
    /**
     * 目标平台:all,h5,android,ios
     * 非必填
     */
    @Pattern(regexp = "^(?i)(()|(all)|(h5)|(android)|(ios))$", message = "platform:值错误,取值范围: all,h5,android,ios,默认all")
    private String platform = "all";
    /**
     * 推送目标
     * 必填
     */
    @NotNull(message = "audience:推送目标不能为空")
    @Valid
    private Audience audience;
    /**
     * 消息
     * 必填
     */
    @NotNull(message = "message:消息内容不能为空")
    @Valid
    private Message message;

    @Data
    @NoArgsConstructor
    public static class Message {
        /**
         * 消息标题
         * 非必填
         */
        private String title;
        /**
         * 消息内容
         * 必填
         */
        @NotEmpty(message = "content:消息内容不能为空")
        private String content;
        /**
         * 消息类型，由调用方自定义内容类型
         * 非必填
         */
        @JsonProperty("content_type")
        private String contentType;
        /**
         * 扩展内容，由调用方自定义扩展
         * 非必填
         */
        private String extras;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class Audience {
        /**
         * 用户uid数组,最多1000个
         */
        @Size(min = 1, max = 1000, message = "uids:一次最少1个,最多1000")
        private List<String> uids;
    }
}
