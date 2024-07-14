package org.kangspace.messagepush.core.http;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RestTemplateFactory工厂，初始化管理连接
 *
 * @author kango2gler@gmail.com
 */
@Slf4j
public class RestTemplateFactory implements InitializingBean {
    public static final String DEFAULT = "default";
    /**
     * rest模板
     */
    private static final Map<String, RestTemplate> REST_TEMPLATE_MAP = new ConcurrentHashMap<>();
    @Resource
    private RestMapProperties restConfig;

    /**
     * 获取RestTemplate
     *
     * @return
     */
    public RestTemplate getRestTemplate() {
        return REST_TEMPLATE_MAP.get(DEFAULT);
    }

    /**
     * 获取RestTemplate
     *
     * @return
     */
    public RestTemplate getRestTemplate(String name) {
        return REST_TEMPLATE_MAP.get(name);
    }

    /**
     * 注册restTemplate
     *
     * @param name
     * @param restProperties
     */
    public synchronized RestTemplate registerRestTemplate(String name, RestProperties restProperties) {
        RestTemplate restTemplate = getRestTemplate(restProperties);
        REST_TEMPLATE_MAP.put(name, restTemplate);
        return restTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("rest连接池初始化开始，配置参数：" + restConfig.getRest());
        restConfig.getRest().forEach((name, restProperties) -> REST_TEMPLATE_MAP.put(name, getRestTemplate(restProperties)));
        if (!REST_TEMPLATE_MAP.containsKey(DEFAULT)) {
            REST_TEMPLATE_MAP.put(DEFAULT, getRestTemplate(new RestProperties()));
        }
        log.info("rest连接池初始化完成，连接池名称：" + REST_TEMPLATE_MAP.keySet());
    }


    private RestTemplate getRestTemplate(RestProperties restProperties) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient(restProperties));
        clientHttpRequestFactory.setConnectTimeout(restProperties.getConnectTimeout());
        clientHttpRequestFactory.setReadTimeout(restProperties.getReadTimeout());
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        modifyDefaultCharset(restTemplate);
        return restTemplate;
    }

    private HttpClient httpClient(RestProperties restProperties) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        //使用Httpclient连接池的方式配置(推荐)，同时支持netty，okHttp以及其他http框架
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        // 最大连接数
        poolingHttpClientConnectionManager.setMaxTotal(restProperties.getMaxTotalConnect());
        // 同路由并发数
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(restProperties.getMaxConnectPerRoute());
        //配置连接池
        httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
        // 重试次数
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(restProperties.getRetryTimes(), Boolean.TRUE));
        //设置长连接保持策略
        if (restProperties.getKeepAliveTargetHost().size() > 0 || restProperties.getDefaultKeepAliveTime() > 0) {
            httpClientBuilder.setKeepAliveStrategy(connectionKeepAliveStrategy(restProperties));
        }
        return httpClientBuilder.build();
    }


    /**
     * 配置长连接保持策略
     *
     * @return 长连接策略
     */
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy(RestProperties restProperties) {
        return (response, context) -> {
            // 设置长连接头
            HeaderElementIterator headerElementIterator = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (headerElementIterator.hasNext()) {
                HeaderElement headerElement = headerElementIterator.nextElement();
                String param = headerElement.getName();
                String value = headerElement.getValue();
                if (StringUtils.isNumericSpace(value) && StringUtils.endsWithIgnoreCase(HttpHeaders.TIMEOUT, param)) {
                    return Duration.ofSeconds(Long.parseLong(value)).toMillis();
                }
            }
            HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            //如果请求目标地址,单独配置了长连接保持时间,使用该配置
            Optional<Map.Entry<String, Integer>> targetMap = restProperties.getKeepAliveTargetHost().entrySet().stream().filter(
                    e -> StringUtils.endsWithIgnoreCase(e.getKey(), target.getHostName())).findAny();
            //否则使用默认长连接保持时间
            return targetMap.map(targetTimeOut -> Duration.ofSeconds(targetTimeOut.getValue()).toMillis()).orElse(Duration.ofSeconds(restProperties.getDefaultKeepAliveTime()).toMillis());
        };
    }

    /**
     * 修改默认的字符集类型为utf-8
     *
     * @param restTemplate 请求客户端
     */
    private void modifyDefaultCharset(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        messageConverters.add(new FormHttpMessageConverter());
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        List<MediaType> supportedMediaTypes = Lists.newArrayList(mappingJackson2HttpMessageConverter.getSupportedMediaTypes());
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes);
        messageConverters.add(mappingJackson2HttpMessageConverter);
        restTemplate.setMessageConverters(messageConverters);
    }
}
