package com.user_service.service;

import common.events.kafka.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserVerificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserVerificationMessage(UserRegisterEvent userRegisterEvent) {
        try {
            CompletableFuture<SendResult<String, Object>> completableFuture =
                    kafkaTemplate.send("user-registration", userRegisterEvent);
            completableFuture.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Send message with offset {} {}: ", userRegisterEvent, result.getRecordMetadata().offset());
                } else {
                    log.warn("Unable to send message: {} {}", userRegisterEvent, "due to: " + exception.getMessage());
                }
            });
        } catch (Exception exception) {
            log.error("Error : {}", exception.getMessage());
        }
    }
}
