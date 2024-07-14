package org.kangspace.messagepush.ws.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 服务启动类
 *
 * @author kango2gler@gmail.com
 * @date 2021/10/28
 */
@EnableFeignClients
@SpringBootApplication
public class MessagePushWsGatewayApplication {

    public static void main(String[] args) {
        setInitProperties();
        SpringApplication.run(MessagePushWsGatewayApplication.class, args);
    }

    /**
     * 设置初始化配置
     *
     * @see reactor.netty.resources.PooledConnectionProvider
     * @see reactor.netty.resources.ConnectionProvider
     * @see org.springframework.cloud.gateway.config.GatewayAutoConfiguration.NettyConfiguration#gatewayHttpClient
     */
    public static void setInitProperties() {
        // reactor.netty.pool.leasingStrategy : netty线程池获取线程策略,默认 fifo;
        // fifo: 取最早释放到连接池的连接(若连接池中的连接长时间未使用,则取出来的连接可能是已经被重置)
        // lifo: 取最近释放到连接池的连接.
        System.setProperty("reactor.netty.pool.leasingStrategy", "lifo");
    }

}

