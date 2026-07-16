package com.commercecore.billing.controller;

import lombok.RequiredArgsConstructor;
import com.commercecore.shared.dto.ApiResponse;
import com.commercecore.billing.dto.PaymentDto;
import com.commercecore.billing.entity.Payment;
import com.commercecore.billing.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("{orderId}")
    public ResponseEntity<ApiResponse<?>> getPaymentByOrderId(@PathVariable("orderId") String orderId) {
        try {
            PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
            return payment != null
                    ? ResponseEntity.ok(new ApiResponse<>(payment, HttpStatus.OK.value()))
                    : ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Unknown order ID: " + orderId, HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                                    e.getMessage(),
                                    HttpStatus.INTERNAL_SERVER_ERROR.value()
                            )
                    );
        }
    }

}
