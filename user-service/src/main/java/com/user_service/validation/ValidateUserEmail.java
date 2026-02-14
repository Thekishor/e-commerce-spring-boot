package com.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailDomainValidator.class)
public @interface ValidateUserEmail {

    String message() default "Disposable email address are not allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
