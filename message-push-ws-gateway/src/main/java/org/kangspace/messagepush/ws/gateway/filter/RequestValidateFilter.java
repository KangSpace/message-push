package org.kangspace.messagepush.ws.gateway.filter;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.constant.ErrorMessageConstants;
import org.kangspace.messagepush.core.constant.MessagePushConstants;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.kangspace.messagepush.ws.gateway.constant.MessagePushWsConstants;
import org.kangspace.messagepush.ws.gateway.domain.dto.response.SessionCenterUserInfoDTO;
import org.kangspace.messagepush.ws.gateway.exception.TokenValidateException;
import org.kangspace.messagepush.ws.gateway.model.MessageRequestParam;
import org.kangspace.messagepush.ws.gateway.util.ExchangeRequestUtils;
import org.kangspace.messagepush.ws.gateway.validation.TokenValidator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

/**
 * <pre>
 * 请求验证过滤器
 * </pre>
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
@Slf4j
public class RequestValidateFilter extends BaseFilter implements GlobalFilter, Ordered {
    /**
     * Token验证器
     */
    private final TokenValidator<SessionCenterUserInfoDTO> tokenValidator;

    public RequestValidateFilter(TokenValidator<SessionCenterUserInfoDTO> tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestPath = exchange.getRequest().getURI().toString();
        MessageRequestParam requestParam = ExchangeRequestUtils.getMessageRequestParam(exchange);
        log.info("请求验证过滤器: ValidateFilter handle start, url: [{}], requestParam: [{}]", requestPath, requestParam);
        // 1. 验证请求,只允许websocket请求访问
        Optional<Mono<Void>> isWebsocket = isWebsocketRequest(exchange);
        if (isWebsocket.isPresent()) {
            log.error("请求验证过滤器: 非Websocket请求,拒绝访问, url: [{}], requestParam: [{}],method: [{}], Connection Header:[{}]",
                    requestPath, requestParam, exchange.getRequest().getMethodValue(),
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONNECTION));
            return isWebsocket.get();
        }
        // 2. 验证请求参数
        Optional<Mono<Void>> validRequestParam = validRequestParam(exchange, requestParam);
        if (validRequestParam.isPresent()) {
            log.error("请求验证过滤器: 参数校验不通过,拒绝访问, url: [{}], requestParam: [{}].", requestPath, requestParam);
            return validRequestParam.get();
        }
        URI uri = exchange.getRequest().getURI();
        URI requestUrl = UriComponentsBuilder.fromUri(uri).build(ServerWebExchangeUtils.containsEncodedParts(uri)).toUri();
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate().uri(requestUrl)
                .header(MessagePushConstants.HTTP_HEADER_UID_KEY, requestParam.getUid());
        exchange = exchange.mutate().request(builder.build()).build();
        // 3. 设置用户信息到exchange attr
        ExchangeRequestUtils.setMessageRequestParam(exchange, requestParam);
        log.info("请求验证过滤器: ValidateFilter handle end, url: [{}], requestParam: [{}].", requestPath, requestParam);
        return chain.filter(exchange);
    }

    /**
     * 验证请求参数
     *
     * @param exchange     {@link ServerWebExchange}
     * @param requestParam {@link MessageRequestParam}
     * @return {@link Optional}
     */
    private Optional<Mono<Void>> validRequestParam(ServerWebExchange exchange, MessageRequestParam requestParam) {
        Optional<Mono<Void>> result = Optional.empty();
        // 参数非空校验
        if (requestParam == null || !StringUtils.hasText(requestParam.getToken()) || !StringUtils.hasText(requestParam.getTokenAppId())) {
            // 模拟用户ID处理
            String mockUId = exchange.getRequest().getHeaders().getFirst(MessagePushWsConstants.MOCK_UID_HEADER);
            // 若存在模拟UID时使用模拟UID
            if (requestParam != null && StringUtils.hasText(mockUId)) {
                requestParam.setUid(mockUId);
            } else {
                String errorMsg = String.format(ErrorMessageConstants.INVALID_REQUEST_PARAM_MSG, "Authorization Bearer, auth-app-id", "null");
                result = Optional.ofNullable(reject(exchange, ResponseEnum.BAD_REQUEST, errorMsg));
            }
        } else {
            try {
                // 通过token验证用户信息
                tokenValidator.valid(requestParam.getTokenAppId(), requestParam.getToken(), ((t) -> requestParam.setUid(t.getUid())));
            } catch (TokenValidateException exception) {
                ResponseEnum responseEnum = null;
                if (TokenValidateException.ExceptionType.INVALID_PARAM.equals(exception.getExceptionType())) {
                    responseEnum = ResponseEnum.BAD_REQUEST;
                } else if (TokenValidateException.ExceptionType.ACCESS_TOKEN_NOT_FOUND.equals(exception.getExceptionType())) {
                    responseEnum = ResponseEnum.UNAUTHORIZED;
                }
                String errorMsg = exception.getMessage();
                log.error("校验参数失败,错误信息:[{}]", errorMsg);
                result = Optional.ofNullable(reject(exchange, responseEnum, errorMsg));
            }

        }
        return result;
    }


    /**
     * <pre>
     * Websocket访问限制过滤器
     * 只允许Http Connection: upgrade的连接通过
     * </pre>
     *
     * @author kango2gler@gmail.com
     * @since 2021/10/27
     */
    private Optional<Mono<Void>> isWebsocketRequest(ServerWebExchange exchange) {
        Optional<Mono<Void>> result = Optional.empty();
        //只允许Websocket:Http Upgrade的请求
        if (!ExchangeRequestUtils.isWebsocketRequest(exchange)) {
            HttpRequest request = exchange.getRequest();
            result = Optional.ofNullable(reject(exchange, ResponseEnum.BAD_REQUEST,
                    String.format(ErrorMessageConstants.INVALID_PROTOCOL_TYPE_MSG,
                            Arrays.toString(MessagePushConstants.WEBSOCKET_PROTOCOLS),
                            request.getURI().getScheme())));
        }
        return result;
    }

    @Override
    public int getOrder() {
        return FilterOrders.VALIDATE_FILTER_ORDER;
    }
}
