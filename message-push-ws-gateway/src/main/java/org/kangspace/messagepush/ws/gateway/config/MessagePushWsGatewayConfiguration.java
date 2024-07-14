package org.kangspace.messagepush.ws.gateway.config;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.core.hash.ConsistencyHashing;
import org.kangspace.messagepush.core.hash.repository.HashRouterRepository;
import org.kangspace.messagepush.core.redis.RedisService;
import org.kangspace.messagepush.ws.gateway.filter.GatewayWebsocketRoutingFilter;
import org.kangspace.messagepush.ws.gateway.filter.RequestValidateFilter;
import org.kangspace.messagepush.ws.gateway.filter.WebsocketReactiveLoadBalancerClientFilter;
import org.kangspace.messagepush.ws.gateway.filter.balancer.LbServiceInstanceChooser;
import org.kangspace.messagepush.ws.gateway.filter.balancer.UIDServiceInstanceChooser;
import org.kangspace.messagepush.ws.gateway.filter.session.WebSocketUserSessionManager;
import org.kangspace.messagepush.ws.gateway.hash.DebugPrintHashingScheduler;
import org.kangspace.messagepush.ws.gateway.hash.RedisHashRouterRepository;
import org.kangspace.messagepush.ws.gateway.nacos.NacosNamingService;
import org.kangspace.messagepush.ws.gateway.validation.PassportSessionValidator;
import org.kangspace.messagepush.ws.gateway.validation.TokenValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;

import java.util.ArrayList;
import java.util.List;


/**
 * 网关配置类
 *
 * @author kango2gler@gmail.com
 * @date 2021/10/28
 * @see ErrorWebFluxAutoConfiguration ErrorWebFluxAutoConfiguration错误处理配置
 */
@Slf4j
public class MessagePushWsGatewayConfiguration {

    /**
     * PassportSession校验器
     *
     * @return
     */
    public PassportSessionValidator tokenValidator() {
        return new PassportSessionValidator();
    }

    /**
     * 验证过滤器
     *
     * @return {@link RequestValidateFilter}
     */
    @Bean
    public RequestValidateFilter validateFilter(TokenValidator tokenValidator) {
        return new RequestValidateFilter(tokenValidator);
    }

    /**
     * WebSocket负载均衡过滤器
     *
     * @param clientFactory {@link LoadBalancerClientFactory}
     * @param properties    {@link LoadBalancerProperties}
     * @return {@link WebsocketReactiveLoadBalancerClientFilter}
     * @see LoadBalancerAutoConfiguration
     * @see org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter
     */
    @Primary
    @Bean
    public ReactiveLoadBalancerClientFilter websocketReactiveLoadBalancerClientFilter(LbServiceInstanceChooser lbServiceInstanceChooser,
                                                                                      LoadBalancerClientFactory clientFactory,
                                                                                      LoadBalancerProperties properties) {
        return new WebsocketReactiveLoadBalancerClientFilter(lbServiceInstanceChooser, clientFactory, properties);
    }

    /**
     * 用户Session管理Bean
     *
     * @return {@link WebSocketUserSessionManager}
     */
    @Bean
    public WebSocketUserSessionManager webSocketUserSessionManager() {
        return new WebSocketUserSessionManager();
    }

    /**
     * Websocket路由过滤器
     *
     * @param webSocketClient  {@link WebSocketClient}
     * @param webSocketService {@link WebSocketService}
     * @param headersFilters   {@link ObjectProvider}
     * @return {@link GatewayWebsocketRoutingFilter}
     */
    @Bean
    public GatewayWebsocketRoutingFilter gatewayWebsocketRoutingFilter(WebSocketClient webSocketClient,
                                                                       WebSocketService webSocketService, ObjectProvider<List<HttpHeadersFilter>> headersFilters,
                                                                       WebSocketUserSessionManager webSocketUserSessionManager) {
        return new GatewayWebsocketRoutingFilter(webSocketClient, webSocketService, headersFilters, webSocketUserSessionManager);
    }

    /**
     * NacosNamingService Instance
     *
     * @param nacosServiceDiscovery {@link NacosServiceDiscovery}
     * @return {@link NacosNamingService}
     */
    @Bean
    public NacosNamingService nacosNamingService(NacosServiceDiscovery nacosServiceDiscovery) {
        return new NacosNamingService(nacosServiceDiscovery);
    }

    /**
     * Redis相关操作Bean
     *
     * @return {@link RedisService}
     */
    @Bean
    public RedisService redisService() {
        return new RedisService();
    }

    /**
     * 一致性Hash数据持久化类
     *
     * @param redisService       {@link RedisService}
     * @param nacosNamingService {@link NacosNamingService}
     * @return {@link HashRouterRepository
     */
    @Bean
    public HashRouterRepository hashRouterRepository(RedisService redisService, NacosNamingService nacosNamingService) {
        return new RedisHashRouterRepository(redisService, nacosNamingService);
    }

    /**
     * Server一致性Hash对象
     * 1. 服务启动时从Redis中获取
     * 2. 监听Nacos心跳获取服务列表,若有变化则Rehash
     *
     * @param hashRouterRepository {@link HashRouterRepository}
     * @return {@link ConsistencyHashing}
     */
    @Bean
    public ConsistencyHashing<ServiceInstance> hashRouter(HashRouterRepository hashRouterRepository) {
        return hashRouterRepository.get();
    }

    /**
     * 负载均衡选择器
     *
     * @param hashRouter {@link ConsistencyHashing}
     * @return {@link LbServiceInstanceChooser}
     */
    @Bean
    public LbServiceInstanceChooser lbServiceInstanceChooser(ConsistencyHashing<ServiceInstance> hashRouter) {
        return new UIDServiceInstanceChooser(hashRouter);
    }

    /**
     * 一致性Hash数据定时打印Bean
     *
     * @return {@link DebugPrintHashingScheduler}
     */
    @Bean
    public DebugPrintHashingScheduler DebugPrintHashingScheduler(ConsistencyHashing<ServiceInstance> hashRouter) {
        return new DebugPrintHashingScheduler(hashRouter);
    }


    /**
     * 使用fastjson代替jackson
     *
     * @return org.springframework.boot.autoconfigure.http.HttpMessageConverters
     */
    @Bean
    public HttpMessageConverters fastJsonConfigure() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue);
        // 日期格式化
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        converter.setFastJsonConfig(fastJsonConfig);
        List<MediaType> mediaTypes = new ArrayList<>(6);
        mediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        mediaTypes.add(MediaType.APPLICATION_CBOR);
        mediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        converter.setSupportedMediaTypes(mediaTypes);
        return new HttpMessageConverters(converter);
    }

    /**
     * 跨域过滤器
     * gateway采用react形式,需要使用reactive包下的UrlBasedCorsConfigurationSource
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(source);
    }
}
