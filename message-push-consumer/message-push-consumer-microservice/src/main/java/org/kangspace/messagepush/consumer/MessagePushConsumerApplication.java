package org.kangspace.messagepush.consumer;


import com.spring4all.swagger.EnableSwagger2Doc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 服务主入口
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/25
 */
@Slf4j
@EnableAsync
@EnableFeignClients
@EnableSwagger2Doc
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MessagePushConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessagePushConsumerApplication.class, args);
    }

}
