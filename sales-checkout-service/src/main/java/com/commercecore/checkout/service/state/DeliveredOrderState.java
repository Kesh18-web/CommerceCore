package com.commercecore.checkout.service.state;

import com.commercecore.checkout.entity.Order;

public class DeliveredOrderState implements OrderState {
    @Override
    public void handleStateChange(Order order) {
        throw new IllegalStateException("Order has already been delivered and cannot be updated.");
    }
}

