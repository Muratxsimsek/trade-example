package org.casestudy.trade.queue;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casestudy.trade.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueueConsumer {

    private final OrderService orderService;

    private final BlockingQueue<OrderMessage> orderQueue = new LinkedBlockingQueue<>();

    public void enqueue(OrderMessage message) {
        orderQueue.add(message);
    }

    @PostConstruct
    public void startConsumer() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                OrderMessage message = orderQueue.take();
                try {
                    orderService.createOrder(message.getCustomerId(), message.getRequest());
                } catch (Exception e) {
                    log.error("Order consumption failed", e);
                }
            }
        });
    }
}
