package org.kangspace.messagepush.ws.gateway.filter;


import com.alibaba.fastjson.JSONObject;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 基础过滤器
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
public class BaseFilter {

    /**
     * 拒绝请求处理,返回apiDto
     *
     * @param exchange {@link ServerWebExchange}
     * @param apiDto   {@link ApiResponse}
     * @return Mono
     */
    protected Mono<Void> reject(ServerWebExchange exchange, ApiResponse apiDto) {
        ServerHttpResponse response = exchange.getResponse();
        HttpStatus responseStatusCode = apiDto.getCode() == 0 ? HttpStatus.OK : HttpStatus.resolve(apiDto.getCode());
        response.setStatusCode(responseStatusCode);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(JSONObject.toJSONBytes(apiDto))));
    }

    /**
     * 拒绝请求,返回apiDto
     *
     * @param exchange     {@link ServerWebExchange}
     * @param responseEnum {@link ResponseEnum}
     * @param message      消息内容
     * @return Mono
     */
    protected Mono<Void> reject(ServerWebExchange exchange, ResponseEnum responseEnum, String message) {
        if (responseEnum == null) {
            return rejectBy500(exchange, message);
        }
        ApiResponse apiDto = new ApiResponse(responseEnum);
        if (StringUtils.hasText(message)) {
            apiDto.setMsg(message);
        }
        return reject(exchange, apiDto);
    }

    /**
     * 拒绝请求,返回500 apiDto
     *
     * @param exchange {@link ServerWebExchange}
     * @param message  消息内容
     * @return Mono
     */
    protected Mono<Void> rejectBy500(ServerWebExchange exchange, String message) {
        ApiResponse apiDto = new ApiResponse();
        apiDto.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiDto.setMsg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        if (StringUtils.hasText(message)) {
            apiDto.setMsg(message);
        }
        return reject(exchange, apiDto);
    }

}
