package com.notification_service.service;

import com.notification_service.constant.NotificationType;
import com.notification_service.entities.Notification;
import com.notification_service.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import common.events.kafkaEvents.OrderEvent;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "order-event", groupId = "order-event-listener", containerFactory = "listenerContainerFactory")
    public void consumeOrderEventFromOrderService(OrderEvent orderEvent) throws MessagingException {
        log.info("Consuming the message from order");

        notificationRepository.save(
                Notification.builder()
                        .notificationType(NotificationType.ORDER_EVENT)
                        .localDateTime(LocalDateTime.now())
                        .userEmail(orderEvent.getEmail())
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
}
