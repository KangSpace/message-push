package org.kangspace.messagepush.ws.core.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kangspace.messagepush.ws.core.domain.model.HttpPushMessageDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Http消息发布者
 *
 * @author kango2gler@gmail.com
 * @see HttpPushMessageConsumer
 * @since 2021/10/30
 */
@Slf4j
public class HttpPushMessagePublisher<T extends HttpPushMessageDTO> {
    /**
     * 消息Queue
     */
    private final BlockingQueue<T> messageQueue = new LinkedBlockingQueue<>();
    private Flux<T> publisher;

    public HttpPushMessagePublisher(HttpPushMessageConsumer<T> messageConsumer) {
        Objects.requireNonNull(messageConsumer, "消息消费者不能为null!");
        publisher = Flux.create(new MessageSinkConsumer<T>()).share();
        publisher.subscribe(messageConsumer);
    }

    /**
     * 发布消息
     *
     * @param message message
     * @return boolean
     */
    public boolean publish(T message) {
        try {
            messageQueue.put(message);
            return true;
        } catch (InterruptedException e) {
            log.error("Http消息发布者:消息发布失败,错误信息:{}", e.getMessage(), e);
        }
        return false;
    }

    /**
     * <pre>
     * 内部消息消费者
     * 为Flux<T> publisher提供数据
     * </pre>
     */
    @NoArgsConstructor
    @Data
    public class MessageSinkConsumer<O> implements Consumer<FluxSink<O>> {
        private final Executor executor = Executors.newSingleThreadExecutor();

        @Override
        public void accept(FluxSink sink) {
            executor.execute(() -> {
                log.info("Http消息发布者: 监听消息开始！");
                while (true) {
                    try {
                        T message = messageQueue.take();
                        sink.next(message);
                    } catch (Exception e) {
                        log.error("Http消息发布者: 消息消费,消费错误:{}", e.getMessage(), e);
                    }
                }
            });
        }
    }

}
