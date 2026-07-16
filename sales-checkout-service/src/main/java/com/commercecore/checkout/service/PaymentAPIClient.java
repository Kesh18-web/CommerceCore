package com.commercecore.checkout.service;

import com.commercecore.shared.dto.ApiResponse;
import com.commercecore.checkout.dto.PaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BILLING-TRANSACTION-SERVICE")
public interface PaymentAPIClient {
    @GetMapping("api/v1/payment/{orderId}")
    ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(@PathVariable("orderId") String orderId);
}
