package com.notification_service.service;

import com.notification_service.constant.EmailTemplates;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import common.events.kafkaEvents.PurchaseResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${user.email}")
    private String email;

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendOrderConfirmationEmail(
            String toEmail,
            String username,
            Long amount,
            String orderNumber,
            List<PurchaseResponse> purchaseResponses
    ) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

        final String template = EmailTemplates.ORDER_EVENT.getTemplate();
        final String subject = EmailTemplates.ORDER_EVENT.getSubject();

        Map<String, Object> variable = new HashMap<>();
        variable.put("username", username);
        variable.put("amount", amount);
        variable.put("orderNumber", orderNumber);
        variable.put("products", purchaseResponses);

        Context context = new Context();
        context.setVariables(variable);

        try {
            String htmlTemplate = templateEngine.process(template, context);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(email);
            helper.setText(htmlTemplate, true);
            javaMailSender.send(message);
        } catch (MessagingException exception) {
            log.warn("WARNING- Can not send email during order processing to:  {}", toEmail);
        } catch (Exception exception) {
            log.error("Error Occurs During Mail Sending Order Processing: {}", exception.getMessage(), exception);
        }
    }

}