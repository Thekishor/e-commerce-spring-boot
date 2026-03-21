package com.notification_service.service;

import com.notification_service.entities.Notification;
import com.notification_service.repository.NotificationRepository;
import common.events.kafka.UserRegisterEvent;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import common.events.kafka.OrderEvent;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 3000, multiplier = 1.5, maxDelay = 15000),
            exclude = {NullPointerException.class}
    )
    @KafkaListener(
            topics = "order-event",
            groupId = "order-event-group",
            containerFactory = "listenerContainerFactoryOrderEvent"
    )
    public void consumeOrderEventFromOrderService(OrderEvent orderEvent) throws MessagingException {
        log.info("Consuming the message from order");

        notificationRepository.save(
                Notification.builder()
                        .notificationType("order-event")
                        .localDateTime(LocalDateTime.now())
                        .userEmail(orderEvent.getEmail())
                        .userId(orderEvent.getUserId())
                        .build()
        );
        emailService.sendOrderConfirmationEmail(
                orderEvent.getEmail(),
                orderEvent.getUsername(),
                orderEvent.getAmount(),
                orderEvent.getOrderNumber(),
                orderEvent.getPurchaseResponseList()
        );
    }

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 3000, multiplier = 1.5, maxDelay = 15000),
            exclude = {NullPointerException.class}
    )
    @KafkaListener(
            topics = "user-registration",
            groupId = "user-event-group",
            containerFactory = "listenerContainerFactoryUserEvent"
    )
    public void consumeUserVerificationUrl(UserRegisterEvent userRegisterEvent) throws MessagingException {
        log.info("Consuming the message from user");

        notificationRepository.save(
                Notification.builder()
                        .notificationType("user-verification")
                        .localDateTime(userRegisterEvent.getLocalDateTime())
                        .userEmail(userRegisterEvent.getEmail())
                        .userId(userRegisterEvent.getUserId())
                        .build()
        );

        emailService.sendUserVerificationEmail(
                userRegisterEvent.getEmail(),
                userRegisterEvent.getUsername(),
                userRegisterEvent.getUrl()
        );
    }

    @DltHandler
    public void listenDLT(OrderEvent orderEvent) {
        log.info("DLT Received -> OrderEvent: {}", orderEvent.getUsername());
    }

    @DltHandler
    public void userEventListenDLT(UserRegisterEvent userRegisterEvent) {
        log.info("DLT Received -> UserEvent: {}", userRegisterEvent.getUserId());
    }
}
