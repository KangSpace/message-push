package org.kangspace.messagepush.ws.core.domain.dto.request;

import lombok.Data;

/**
 * 登录命令Data内容DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/29
 */
@Data
public class LoginReqDataDTO {
    /**
     * 为当前业务分配的应用ID(与REST PUSH API使用的应用ID相同)
     */
    private String appKey;
    /**
     * 当前平台,值为[H5,Android,iOS],按客户端类型设置
     */
    private String platform;
}
