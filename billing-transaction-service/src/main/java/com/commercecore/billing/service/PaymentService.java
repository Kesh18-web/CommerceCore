package com.commercecore.billing.service;


import com.commercecore.shared.dto.order.OrderEvent;
import com.commercecore.billing.dto.PaymentDto;
import com.commercecore.billing.entity.Payment;
import com.commercecore.billing.entity.PaymentStatus;

public interface PaymentService {
    void createPayment(OrderEvent orderEvent);
    PaymentDto getPaymentByOrderId(String orderId);
    void updateStatusPayment(String orderId, PaymentStatus status);
    void refundPayment(String orderId);
}
