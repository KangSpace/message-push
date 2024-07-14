package org.kangspace.messagepush.ws.core;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import reactor.core.publisher.Flux;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 公共测试类型
 *
 * @author kango2gler@gmail.com
 * @since 2021/8/7
 */
@RunWith(JUnit4.class)
public class FluxTest {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    /**
     * Flux测试
     * https://stackoverflow.com/questions/54248336/springboot2-webflux-websocket
     */
    @Test
    public void fluxTest() throws InterruptedException {
        Flux<String> publisher = Flux.create(sink -> {
            try {
                while (true) {
                    sink.next(queue.take());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        new Timer().schedule(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {
                String data = "beat:" + System.currentTimeMillis();
                queue.put(data);
            }
        }, 0, 1000L);

        publisher.subscribe(this::subscribe);
        Thread.sleep(1 * 60 * 1000L);
    }

    public void subscribe(String temp) {
        System.out.println("subscribe: thread:" + Thread.currentThread().getName() + ", data:" + temp);
    }


}
