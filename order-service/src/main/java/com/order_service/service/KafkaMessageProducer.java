package com.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import common.events.kafkaEvents.OrderEvent;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderEventMessage(OrderEvent orderEvent) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("order-event", orderEvent);
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Send message with offset {} {}: ", orderEvent, result.getRecordMetadata().offset());
                } else {
                    log.warn("Unable to send message: {} {}", orderEvent, "due to: " + exception.getMessage());
                }
            });
        } catch (Exception exception) {
            log.error("Error : {}", exception.getMessage());
        }
    }
}
