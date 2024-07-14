package org.kangspace.messagepush.rest.core.auth;


import com.alibaba.fastjson.JSONObject;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.kangspace.messagepush.core.util.HttpUtils;
import org.kangspace.messagepush.rest.core.constant.AppThreadLocal;
import org.kangspace.messagepush.rest.core.utils.AppGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

/**
 * 接口请求过滤器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Component
@Aspect
public class AuthRequestAop {

    /**
     * 检查认证信息
     * 拦截所有Controller请求,校验包含{@link ApiAuthentication}的方法
     */
    @Around("execution(public * org.kangspace.messagepush.rest.controller.*.*(..))")
    public Object checkAuthentication(ProceedingJoinPoint pjp) throws Throwable {
        // 获取当前方法
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Annotation apiAuthentication = methodSignature.getMethod().getAnnotation(ApiAuthentication.class);
        if (apiAuthentication != null) {
            // 获取当前请求
            ServletRequestAttributes servlet = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
            HttpServletRequest request = servlet.getRequest();
            HttpServletResponse response = servlet.getResponse();
            // 判断请求头
            if (!validHttpBasic(request)) {
                // 截断请求
                httpBasicValidFailed(response);
                return null;
            }
            // 请求继续
            AppThreadLocal.setAppKey(HttpUtils.getBasicAuth(request).getUsername());
        }
        Object result = pjp.proceed();
        AppThreadLocal.reset();
        return result;
    }

    /**
     * 验证HttpBasic
     * (此处验证自定义AppKey,AppSecret)
     *
     * @param request HttpServletRequest
     * @return boolean 验证成功/失败
     * @see AppGenerator
     */
    public boolean validHttpBasic(HttpServletRequest request) {
        HttpUtils.HttpBasicAuth basicAuth = HttpUtils.getBasicAuth(request);
        if (basicAuth != null) {
            String username = basicAuth.getUsername();
            String password = basicAuth.getPassword();
            return AppGenerator.validAppInfo(username, password);
        }
        return false;
    }

    /**
     * HttpBasic 鉴权失败响应
     *
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    private void httpBasicValidFailed(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        ApiResponse result = new ApiResponse(ResponseEnum.FORBIDDEN.getValue(), "invalid Authorization");
        response.getWriter().print(JSONObject.toJSONString(result));
    }

}
