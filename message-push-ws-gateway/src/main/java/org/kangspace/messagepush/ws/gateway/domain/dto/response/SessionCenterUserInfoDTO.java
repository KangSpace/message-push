package org.kangspace.messagepush.ws.gateway.domain.dto.response;

import lombok.Data;

/**
 * SessionCenter用户信息
 *
 * @author kango2gler@gmail.com
 * @since 2021/11/5
 */
@Data
public class SessionCenterUserInfoDTO {
    /**
     * 头像
     */
    private String avatar;
    /**
     * 性别
     */
    private String gender;
    /**
     * 用户ID
     */
    private String uid;
    /**
     * 真实姓名
     */
    private String realname;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 手机号
     */
    private String mobile;
}
