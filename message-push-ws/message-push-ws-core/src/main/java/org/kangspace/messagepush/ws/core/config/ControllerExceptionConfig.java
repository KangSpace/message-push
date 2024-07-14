package org.kangspace.messagepush.ws.core.config;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
@Slf4j
@Configuration
public class ControllerExceptionConfig {

    /**
     * 接口异常处理
     */
    @RestControllerAdvice
    public static class ControllerExceptionHandleAdvice {
        @ExceptionHandler
        public Mono<ApiResponse> handler(ServerWebExchange exchange, Exception e) {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            log.error("Controller 处理异常,url:{},错误信息:{}", request.getURI(), e.getMessage(), e);
            ApiResponse responseDTO;
            int responseStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            if (e instanceof WebExchangeBindException) {
//                responseDTO = new ApiResponse(ResponseEnum.BAD_REQUEST);
//                responseDTO.setMsg(responseDTO.getMsg() + ":输入参数内容错误,请确认后重试.");
                return exceptionHandler((WebExchangeBindException) e);
            } else if (e instanceof HttpRequestMethodNotSupportedException) {
                responseDTO = new ApiResponse(ResponseEnum.METHOD_NOT_ALLOWED);
                responseStatus = HttpStatus.BAD_REQUEST.value();
            } else if (e instanceof HttpMessageNotReadableException) {
                responseDTO = new ApiResponse(ResponseEnum.BAD_REQUEST);
                responseDTO.setMsg(responseDTO.getMsg() + ":输入参数(JSON)格式错误,请确认后重试.");
                responseStatus = HttpStatus.BAD_REQUEST.value();
            } else if (e instanceof HttpMessageConversionException) {
                responseDTO = new ApiResponse(ResponseEnum.BAD_REQUEST);
                responseStatus = HttpStatus.BAD_REQUEST.value();
            } else {
                responseDTO = new ApiResponse(ResponseEnum.INTERNAL_SERVER_ERROR.getValue(), ResponseEnum.INTERNAL_SERVER_ERROR.getReasonPhrase());
            }
            response.setRawStatusCode(responseStatus);
            return Mono.just(responseDTO);
        }

        /**
         * 参数错误处理
         *
         * @param exception WebExchangeBindException
         * @return ApiResponse
         */
        @ResponseBody
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public Mono<ApiResponse> exceptionHandler(WebExchangeBindException exception) {
            BindingResult result = exception.getBindingResult();
            StringBuilder sb = new StringBuilder("参数错误:");
            if (result.hasErrors()) {
                List<ObjectError> errors = result.getAllErrors();
                if (errors != null) {
                    sb.append(errors.stream().map(p -> {
                        FieldError fieldError = (FieldError) p;
                        log.warn("Bad Request Parameters: dto entity [{}],field [{}],message [{}]", fieldError.getObjectName(), fieldError.getField(), fieldError.getDefaultMessage());
                        return fieldError.getDefaultMessage();
                    }).collect(Collectors.joining(",")));
                }
            }
            return Mono.just(new ApiResponse(ResponseEnum.BAD_REQUEST.getValue(), sb.toString()));
        }
    }
}
