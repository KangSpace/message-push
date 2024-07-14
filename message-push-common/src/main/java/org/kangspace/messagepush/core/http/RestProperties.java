package org.kangspace.messagepush.core.http;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author kango2gler@gmail.com
 * @description rest配置类，需全部由默认值
 **/
@Setter
@Getter
public class RestProperties {

    /**
     * 连接池的最大连接数
     */
    private Integer maxTotalConnect = 50;
    /**
     * 同路由的并发数
     */
    private Integer maxConnectPerRoute = 200;

    /**
     * 客户端和服务器建立连接超时，默认2s
     */
    private Integer connectTimeout = 2000;
    /**
     * 指客户端从服务器读取数据包的间隔超时时间,不是总读取时间，默认30s
     */
    private Integer readTimeout = 10000;

    /**
     * 编码格式
     */
    private String charset = StandardCharsets.UTF_8.name();
    /**
     * 重试次数,默认2次
     */
    private Integer retryTimes = 1;
    /**
     * 从连接池获取连接的超时时间,不宜过长,单位ms
     */
    private Integer connectionRequestTimeout = 200;
    /**
     * 针对不同的地址,特别设置不同的长连接保持时间
     */
    private Map<String, Integer> keepAliveTargetHost = Maps.newHashMap();
    /**
     * 针对不同的地址,特别设置不同的长连接保持时间,单位 s，若defaultKeepAliveTime为0且keepAliveTargetHost大小为空，则不启用长连接策略
     */
    private Integer defaultKeepAliveTime = 60;


}
