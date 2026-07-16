package com.commercecore.checkout.service.state;

import com.commercecore.checkout.entity.Order;
import com.commercecore.checkout.entity.OrderStatus;

public class ProcessingOrderState implements OrderState{

    @Override
    public void handleStateChange(Order order) {
        order.setStatus(OrderStatus.SHIPPING.getLabel());
    }
}
