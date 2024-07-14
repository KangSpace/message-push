package org.kangspace.messagepush.ws.gateway.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SessionCenter 用户信息请求参数DTO
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionCenterUserInfoParamDTO {
    /**
     * AccessToken
     */
    private String accessToken;
    /**
     * 平台
     */
    private String platForm;
    /**
     * 应用ID
     */
    private String appId;
}
