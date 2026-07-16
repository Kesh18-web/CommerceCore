package com.commercecore.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ERole {
    ADMINISTRATOR("Administrator"),
    EMPLOYEE("Employee"),
    CUSTOMER("Customer");

    private final String label;
}
