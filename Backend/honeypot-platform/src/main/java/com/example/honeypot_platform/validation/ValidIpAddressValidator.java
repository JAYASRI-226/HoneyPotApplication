package com.example.honeypot_platform.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidIpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return IpAddresses.isValid(value);
    }
}
