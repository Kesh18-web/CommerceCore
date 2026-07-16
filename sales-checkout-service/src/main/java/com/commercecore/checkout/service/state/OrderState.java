package com.commercecore.checkout.service.state;

import com.commercecore.checkout.entity.Order;

public interface OrderState {
    void handleStateChange(Order order);
}
