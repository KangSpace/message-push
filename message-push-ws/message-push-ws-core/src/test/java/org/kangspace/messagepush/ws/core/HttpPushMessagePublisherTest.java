package org.kangspace.messagepush.ws.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kangspace.messagepush.rest.api.dto.request.MessagePushRequestDTO;
import org.kangspace.messagepush.ws.core.domain.model.HttpPushMessageDTO;
import org.kangspace.messagepush.ws.core.websocket.HttpPushMessageConsumer;
import org.kangspace.messagepush.ws.core.websocket.HttpPushMessagePublisher;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Http消息发布者测试
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/30
 */
@RunWith(JUnit4.class)
@Slf4j
public class HttpPushMessagePublisherTest {

    private HttpPushMessagePublisher publisher;

    @Before
    public void init() {
        this.publisher = new HttpPushMessagePublisher(new HttpPushMessageConsumer(null));
    }

    @Test
    public void test() throws InterruptedException {
        new Timer().schedule(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {
                String data = "beat:" + System.currentTimeMillis();
                HttpPushMessagePublisher publisher = HttpPushMessagePublisherTest.this.publisher;
                HttpPushMessageDTO message = new HttpPushMessageDTO();
                MessagePushRequestDTO.Message coreMsg = new MessagePushRequestDTO.Message();
                coreMsg.setContent(data);
                message.setMessage(coreMsg);
                publisher.publish(message);
            }
        }, 0, 1000L);
        Thread.sleep(1 * 60 * 1000L);
    }
}
