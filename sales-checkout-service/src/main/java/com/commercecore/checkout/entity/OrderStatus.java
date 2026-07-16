package com.commercecore.checkout.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPING("Shipping"),
    DELIVERED("Delivered"),
    CANCELED("Canceled");

    public final String label;
}
