package com.notification_service.constant;

import lombok.Getter;

@Getter
public enum EmailTemplates {

    ORDER_EVENT("order-event.html", "Order Confirmation"),
    VERIFICATION_EVENT("verification-event.html", "Email Verification");

    private final String template;
    private final String subject;

    EmailTemplates(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
