package com.notification_service.service;

import com.notification_service.constant.NotificationEvent;
import com.notification_service.constant.NotificationStatus;
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
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import common.events.kafka.OrderEvent;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.ConnectException;
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
            include = {ConnectException.class},
            exclude = {
                    NullPointerException.class,
                    IllegalArgumentException.class,
                    MethodArgumentNotValidException.class
            }
    )
    @KafkaListener(
            topics = "order-event",
            groupId = "order-event-group",
            containerFactory = "listenerContainerFactoryOrderEvent"
    )
    public void consumeOrderEventFromOrderService(
            OrderEvent orderEvent,
            Acknowledgment acknowledgment
    ) throws MessagingException {
        log.info("Consuming the message from order");

        if (notificationRepository.existsByEventId(orderEvent.getEventId())) {
            log.info("Duplicate order event received: {}", orderEvent.getEventId());
        }

        Notification notification = Notification.builder()
                .notificationEvent(NotificationEvent.ORDER_EVENT)
                .notificationStatus(NotificationStatus.PROCESSING)
                .eventId(orderEvent.getEventId())
                .localDateTime(LocalDateTime.now())
                .userEmail(orderEvent.getEmail())
                .userId(orderEvent.getUserId())
                .build();
        notificationRepository.save(notification);

        try {
            emailService.sendOrderConfirmationEmail(
                    orderEvent.getEmail(),
                    orderEvent.getUsername(),
                    orderEvent.getAmount(),
                    orderEvent.getOrderNumber(),
                    orderEvent.getPurchaseResponseList()
            );

            log.info("Email sending is completed to user: {}", orderEvent);

            notification.setNotificationStatus(NotificationStatus.SUCCESS);
            notificationRepository.save(notification);
            log.info("Processing is completed successfully: {}", notification);

            log.info("Manually Acknowledge to the kafka offset");
            acknowledgment.acknowledge();
            log.info("Acknowledge done");
        } catch (Exception exception) {
            notification.setNotificationStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.info(exception.getMessage());
        }
    }

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 3000, multiplier = 1.5, maxDelay = 15000),
            include = {ConnectException.class},
            exclude = {
                    NullPointerException.class,
                    IllegalArgumentException.class,
                    MethodArgumentNotValidException.class
            }
    )
    @KafkaListener(
            topics = "user-registration",
            groupId = "user-event-group",
            containerFactory = "listenerContainerFactoryUserEvent"
    )
    public void consumeUserVerificationUrl(
            UserRegisterEvent userRegisterEvent,
            Acknowledgment acknowledgment
    ) throws MessagingException {
        log.info("Consuming the message from user");

        if (notificationRepository.existsByEventId(userRegisterEvent.getEventId())) {
            log.info("Duplicate user register event received: {}", userRegisterEvent.getEventId());
        }

        Notification notification = Notification.builder()
                .notificationEvent(NotificationEvent.USER_VERIFICATION_EVENT)
                .eventId(userRegisterEvent.getEventId())
                .notificationStatus(NotificationStatus.PROCESSING)
                .localDateTime(userRegisterEvent.getLocalDateTime())
                .userEmail(userRegisterEvent.getEmail())
                .userId(userRegisterEvent.getUserId())
                .build();

        notificationRepository.save(notification);

        try {
            emailService.sendUserVerificationEmail(
                    userRegisterEvent.getEmail(),
                    userRegisterEvent.getUsername(),
                    userRegisterEvent.getUrl()
            );
        } catch (Exception exception) {
            notification.setNotificationStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            log.info(exception.getMessage());
        }
    }

    @DltHandler
    public void listenDLT(OrderEvent orderEvent) {
        log.info("DLT Received -> OrderEvent: {}", orderEvent);
    }

    @DltHandler
    public void userEventListenDLT(UserRegisterEvent userRegisterEvent) {
        log.info("DLT Received -> UserEvent: {}", userRegisterEvent);
    }
}
