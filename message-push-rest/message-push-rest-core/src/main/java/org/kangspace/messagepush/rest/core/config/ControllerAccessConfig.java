package org.kangspace.messagepush.rest.core.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Controller 请求日志
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/17
 */
@Component
@Order
@Aspect
@Slf4j
public class ControllerAccessConfig extends OncePerRequestFilter implements Ordered {

    /**
     * 获取请求参数
     *
     * @param request 请求
     * @return params: a=b,c=d
     */
    public static String getRequestParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> enu = request.getParameterNames();
        //获取请求参数
        while (enu.hasMoreElements()) {
            String name = enu.nextElement();
            sb.append(name).append("=").append(request.getParameter(name));
            if (enu.hasMoreElements()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean isSkipUrl = skipUrl(request);
        if (isSkipUrl) {
            return;
        }
        StopWatch sw = new StopWatch();
        sw.start();
        String requestURL = request.getRequestURL().toString();
        String requestMethod = request.getMethod();
        String queryString = request.getQueryString();
        requestURL += StringUtils.isNotBlank(queryString) ? ("?" + queryString) : "";
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(requestWrapper, responseWrapper);
        String body = getRequestBody(requestWrapper);
        String responseBody = getResponseBody(responseWrapper);
        responseWrapper.copyBodyToResponse();
        sw.stop();
        double costTime = sw.getTotalTimeSeconds();
        log.info("RECEIVE<== " + requestMethod + " " + requestURL + ""
                + " Request Params：" + getRequestParams(request) + " "
                + " Request Body：" + body + "\n"
                + "<== 请求耗时：" + costTime + "s\n"
                + "<== 响应内容：" + responseBody);
    }

    /**
     * 忽略url
     *
     * @param request
     * @return
     */
    private boolean skipUrl(HttpServletRequest request) {
        String url = request.getRequestURI();
        return url.indexOf("/swagger") > -1;
    }

    /**
     * 获取请求体
     *
     * @param request request
     * @return request body
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                return payload.replaceAll("\\n", "");
            }
        }
        return "";
    }

    /**
     * 获取响应体
     *
     * @param response response
     * @return response body
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                return payload.replaceAll("\\n", "");
            }
        }
        return "";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 8;
    }
}
