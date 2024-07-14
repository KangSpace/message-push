package org.kangspace.messagepush.ws.gateway.feign;


import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.ws.gateway.domain.dto.request.SessionCenterUserInfoParamDTO;
import org.kangspace.messagepush.ws.gateway.domain.dto.response.SessionCenterUserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Passport SessionCenter相关接口
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/05
 */
@FeignClient(value = "passport-session-center", path = "/", url = "${message-push.gateway.passport-session-url}", fallback = DefaultFeignFallBack.class)
public interface PassportSessionFeignApi {

    /**
     * 通过AccessToken获取用户信息
     *
     * @param paramDto
     * @return
     */
    @PostMapping()
    ApiResponse<SessionCenterUserInfoDTO> userLoginInfo(@RequestBody SessionCenterUserInfoParamDTO paramDto);

}
