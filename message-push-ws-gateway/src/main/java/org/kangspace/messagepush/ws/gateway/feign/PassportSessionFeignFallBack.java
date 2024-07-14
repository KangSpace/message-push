package org.kangspace.messagepush.ws.gateway.feign;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.ws.gateway.domain.dto.request.SessionCenterUserInfoParamDTO;
import org.kangspace.messagepush.ws.gateway.domain.dto.response.SessionCenterUserInfoDTO;
import org.springframework.stereotype.Service;

/**
 * Passport SessionCenter 相关接口feign降级处理
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@Slf4j
@Service
public class PassportSessionFeignFallBack implements PassportSessionFeignApi {

    @Override
    public ApiResponse<SessionCenterUserInfoDTO> userLoginInfo(SessionCenterUserInfoParamDTO paramDto) {
        log.error("通过AccessToken获取Passport SessionCenter用户信息失败,参数:[{}]", paramDto);
        return null;
    }
}
