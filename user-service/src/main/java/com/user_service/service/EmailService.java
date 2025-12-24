package com.user_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${user.email}")
    private String email;

    public void sendEmailVerificationLink(String toEmail, String verificationLink, String username) throws MessagingException {
        String content = "<p>Dear " + username + ",</p>"
                + "<p> Thank you for registering.</p>"
                + "<p>Please confirm your email address by clicking the button below:</p>"
                + "<p><a href=\"" + verificationLink + "\" "
                + "style=\"background:#007bff;color:#fff;padding:10px 20px;"
                + "border-radius:5px;text-decoration:none;\">Verify Email</a></p>"
                + "<p>This link will expire in 1 hours.</p>"
                + "<p>Regards,<br>Kishor Pandey</p>";

        String subject = "Please verify your registration";
        emailSend(toEmail, subject, content);
    }

    public void sendPasswordResetLink(String toEmail, String passwordResetLink, String username) {
        String content = "<p>Dear " + username + ",</p>"
                + "<p>Please click the link below to reset your password:</p>"
                + "<p><a href=\"" + passwordResetLink + "\" "
                + "style=\"background:#007bff;color:#fff;padding:10px 20px;"
                + "border-radius:5px;text-decoration:none;\">Reset Password</a></p>"
                + "<p>This link will expire in 1 hours.</p>"
                + "<p>Regards,<br>Kishor Pandey</p>";

        String subject = "Reset your password";
        emailSend(toEmail, subject, content);
    }

    @Async
    public void emailSend(String toEmail, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(email);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException exception) {
            log.error("Failed to send email:", exception);
            throw new IllegalStateException("Failed to send email");
        }
    }
}
