package org.kangspace.messagepush.consumer.core.config;


import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.dto.response.ApiResponse;
import org.kangspace.messagepush.core.enums.ResponseEnum;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kango2gler@gmail.com
 * @since 2021/10/26
 */
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    /**
     * 设置允许跨域
     *
     * @param registry registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        super.addCorsMappings(registry);
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("POST", "GET", "OPTIONS", "PUT", "PATCH", "DELETE")
                .allowedOrigins("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }

    /**
     * 接口异常处理
     */
    @RestControllerAdvice
    public static class ControllerExceptionHandleAdvice {
        @ExceptionHandler
        public ApiResponse handler(HttpServletRequest request, HttpServletResponse response, Exception e) {
            log.error("Controller 处理异常,url:{},错误信息:{}", request.getRequestURL(), e.getMessage(), e);
            ApiResponse responseDTO;
            int responseStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();
            if (e instanceof HttpRequestMethodNotSupportedException) {
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
            response.setStatus(responseStatus);
            return responseDTO;
        }

        /**
         * 参数错误处理
         *
         * @param exception exception
         * @return ApiResponse
         */
        @ResponseBody
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ApiResponse exceptionHandler(MethodArgumentNotValidException exception) {
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
            return new ApiResponse(ResponseEnum.BAD_REQUEST.getValue(), sb.toString());
        }
    }
}
