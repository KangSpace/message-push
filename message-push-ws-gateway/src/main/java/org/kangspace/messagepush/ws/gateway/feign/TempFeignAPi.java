package org.kangspace.messagepush.ws.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author kango2gler@gmail.com
 * @since 2021/8/9
 */
@FeignClient(value = "temp", path = "/", fallback = DefaultFeignFallBack.class)
public interface TempFeignAPi {

}