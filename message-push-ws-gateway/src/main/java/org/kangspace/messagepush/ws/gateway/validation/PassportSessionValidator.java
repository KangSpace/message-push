package org.kangspace.messagepush.ws.gateway.validation;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.kangspace.messagepush.ws.gateway.domain.dto.request.SessionCenterUserInfoParamDTO;
import org.kangspace.messagepush.ws.gateway.domain.dto.response.SessionCenterUserInfoDTO;
import org.kangspace.messagepush.ws.gateway.exception.TokenValidateException;
import org.kangspace.messagepush.ws.gateway.feign.PassportSessionFeignApi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Consumer;

/**
 * Passport session验证器
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/5
 */
@Slf4j
@Service
public class PassportSessionValidator implements TokenValidator<SessionCenterUserInfoDTO> {
    /**
     * token 元素个数
     */
    private final int TOKEN_ELEMENT_LEN = 2;
    /**
     * token 元素分隔符
     * 格式: pc:xxxxxx,
     */
    private final String TOKEN_ELEMENT_DELIMITER = ":";
    /**
     * 移动端token 元素分割符
     * 格式: md5|mobile:xxxxx
     */
    private final String MOBILE_TOKEN_ELEMENT_DELIMITER = "\\|";

    @Resource
    private PassportSessionFeignApi passportSessionFeignApi;

    @Override
    public SessionCenterUserInfoDTO valid(String authAppId, String token, Consumer<SessionCenterUserInfoDTO> callback) {
        if (isMobileToken(token)) {
            token = getAccessTokenFromMobileToken(token);
        }
        String[] platformAndToken = token.split(TOKEN_ELEMENT_DELIMITER);
        if (TOKEN_ELEMENT_LEN != platformAndToken.length) {
            String errorMsg = "token格式错误,应该为[platform:accessToken]格式,token:" + token;
            log.error(errorMsg);
            throw new TokenValidateException(TokenValidateException.ExceptionType.INVALID_PARAM, errorMsg);
        }
        String platform = platformAndToken[0];
        String accessToken = platformAndToken[1];
        SessionCenterUserInfoParamDTO paramDTO = new SessionCenterUserInfoParamDTO(accessToken, platform, authAppId);
        ApiResponse<SessionCenterUserInfoDTO> userLoginInfoResp = passportSessionFeignApi.userLoginInfo(paramDTO);
        if (userLoginInfoResp != null) {
            if (ResponseEnum.OK.getValue() == userLoginInfoResp.getCode()) {
                SessionCenterUserInfoDTO dto = userLoginInfoResp.getData();
                if (dto != null && callback != null) {
                    callback.accept(dto);
                }
                return dto;
            }
            String errorMsg = "获取PassportSession信息不存在,response:" + userLoginInfoResp;
            log.error(errorMsg);
            throw new TokenValidateException(TokenValidateException.ExceptionType.ACCESS_TOKEN_NOT_FOUND, errorMsg);
        } else {
            String errorMsg = "获取PassportSession信息请求失败,response:" + userLoginInfoResp;
            log.error(errorMsg);
            throw new TokenValidateException(TokenValidateException.ExceptionType.SERVER_ERROR, errorMsg);
        }
    }

    /**
     * 是否移动端格式token
     *
     * @return boolean
     */
    private boolean isMobileToken(String token) {
        return token.split(MOBILE_TOKEN_ELEMENT_DELIMITER).length == 2;
    }

    /**
     * 获取移动端token中的AccessToken
     *
     * @return access_token
     */
    private String getAccessTokenFromMobileToken(String token) {
        return token.split(MOBILE_TOKEN_ELEMENT_DELIMITER)[1];
    }
}
