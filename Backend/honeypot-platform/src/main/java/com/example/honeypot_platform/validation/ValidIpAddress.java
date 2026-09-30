package com.example.honeypot_platform.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIpAddressValidator.class)
public @interface ValidIpAddress {

    String message() default "Must be a valid IPv4 or IPv6 address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
