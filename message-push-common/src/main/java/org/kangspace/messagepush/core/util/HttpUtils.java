package org.kangspace.messagepush.core.util;

import lombok.Data;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletRequest;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Http相关工具类
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
public class HttpUtils {
    /**
     * Http Basic认证元素个数(即username,password)
     */
    public static final int HTTP_BASIC_AUTH_ELE_NUM = 2;
    /**
     * Http Basic认证头的值前缀
     */
    public static final String HTTP_BASIC_AUTH_VALUE_PREFIX = "Basic ";
    /**
     * Http Bearer认证头的值前缀
     */
    public static final String HTTP_BEARER_TOKEN_VALUE_PREFIX = "Bearer ";
    public static final String HTTP_SCHEMA = "http://";
    public static final String HTTPS_SCHEMA = "https://";
    /**
     * multipart 媒体类型
     */
    public static List<String> MULTI_PART_MEDIA_TYPES = Arrays.asList(MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.MULTIPART_MIXED_VALUE,
            MediaType.MULTIPART_RELATED_VALUE);

    /**
     * 获取请求头中的Basic认证信息
     *
     * @param request {@link HttpServletRequest}
     * @return {@link HttpBasicAuth}
     */
    public static HttpBasicAuth getBasicAuth(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(HTTP_BASIC_AUTH_VALUE_PREFIX)) {
            try {
                byte[] base64Dec = Base64Utils.decode(authorization.substring(HTTP_BASIC_AUTH_VALUE_PREFIX.length()).getBytes(StandardCharsets.UTF_8));
                String credentialsString = new String(base64Dec, StandardCharsets.UTF_8);
                String[] credentials = credentialsString.split(":");
                if (credentials.length == HTTP_BASIC_AUTH_ELE_NUM) {
                    return new HttpBasicAuth(credentials[0], credentials[1]);
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    /**
     * 获取BearerToken
     *
     * @param request HttpServletRequest
     * @return Bearer Token
     */
    public static String getHttpBearerToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(HTTP_BEARER_TOKEN_VALUE_PREFIX)) {
            return authorization.substring(HTTP_BEARER_TOKEN_VALUE_PREFIX.length());
        }
        return authorization;
    }

    /**
     * 是否是文件上传类型
     *
     * @param contentType contentTYpe
     * @return boolean
     */
    public static boolean isMultipartContent(String contentType) {
        if (StringUtils.hasText(contentType)) {
            return MULTI_PART_MEDIA_TYPES.stream().anyMatch(contentType::contains);
        }
        return false;
    }


    /**
     * 获取请求头
     *
     * @param request {@link ServerHttpRequest}
     * @return headers
     */
    public static String requestHeader(ServerHttpRequest request) {
        return request.getHeaders().toString();
    }

    /**
     * 获取请求参数
     *
     * @param request {@link ServerHttpRequest}
     * @return headers
     */
    public static String requestParams(ServerHttpRequest request) {
        return request.getQueryParams().toString();
    }

    /**
     * 获取请求体RequestBody(除文件上传外)
     *
     * @return request body
     */
    public static String resolveBody(ServerHttpRequest request) {
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (!isMultipartContent(contentType)) {
            //获取请求体
            Flux<DataBuffer> body = request.getBody();
            AtomicReference<String> bodyRef = new AtomicReference<>();
            body.subscribe(buffer -> {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
                DataBufferUtils.release(buffer);
                bodyRef.set(charBuffer.toString());
            });
            //获取request body
            return bodyRef.get();
        }
        return "";
    }

    /**
     * 适配schema,自动填充http://
     *
     * @param uri uri
     * @return 带 http://的uri
     */
    public static String fitSchema(String uri) {
        if (!StringUtils.hasText(uri)) {
            return uri;
        }
        String lowerUri = uri.toLowerCase();
        if (!lowerUri.startsWith(HTTP_SCHEMA) && !lowerUri.startsWith(HTTPS_SCHEMA)) {
            return HTTP_SCHEMA + uri;
        }
        return uri;
    }


    /**
     * 获取BearerToken
     *
     * @param headers getHttpBearerToken
     * @return Bearer Token
     */
    public static String getHttpBearerToken(HttpHeaders headers) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith(HTTP_BEARER_TOKEN_VALUE_PREFIX)) {
            return authorization.substring(HTTP_BEARER_TOKEN_VALUE_PREFIX.length());
        }
        return null;
    }

    /**
     * Http Basic认证头
     */
    @Data
    public static class HttpBasicAuth {
        private String username;
        private String password;

        public HttpBasicAuth() {
        }

        public HttpBasicAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
