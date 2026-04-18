package com.user_service.service;

import common.events.kafka.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserVerificationMessage(UserEvent userEvent) {
        try {
            CompletableFuture<SendResult<String, Object>> completableFuture = kafkaTemplate
                    .send("user-verification-topic",
                            String.valueOf(userEvent.getUserId()),
                            userEvent
                    );

            completableFuture.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Send user verification message with offset {} {}: ",
                            userEvent, result.getRecordMetadata().offset());
                } else {
                    log.warn("Unable to send user verification message: {} {}", userEvent,
                            "due to: " + exception.getMessage());
                }
            });
        } catch (Exception exception) {
            log.error("Error occurs during send user verification message: {}",
                    exception.getMessage());
        }
    }

    public void sendUserPasswordResetMessage(UserEvent userEvent) {
        try {
            CompletableFuture<SendResult<String, Object>> cf = kafkaTemplate
                    .send("user-password-reset-topic",
                            String.valueOf(userEvent.getUserId()),
                            userEvent
                    );

            cf.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Send password reset message with offset {} {}:",
                            userEvent, result.getRecordMetadata());
                } else {
                    log.warn("Unable to send user password reset message: {} {}", userEvent,
                            "due to:" + exception.getMessage());
                }
            });
        } catch (Exception exception) {
            log.error("Error occurs during  send user password reset message: {}",
                    exception.getMessage());
        }
    }
}
