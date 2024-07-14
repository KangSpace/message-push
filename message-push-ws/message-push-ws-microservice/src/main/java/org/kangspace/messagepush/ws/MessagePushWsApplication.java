package org.kangspace.messagepush.ws;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 服务主入口
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Slf4j
@EnableAspectJAutoProxy
@EnableFeignClients
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MessagePushWsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagePushWsApplication.class, args);
    }

}
