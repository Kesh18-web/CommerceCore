package com.commercecore.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.commercecore.shared.dto.order.OrderDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private OrderDTO orderDTO;
    private PaymentDto paymentDto;
}
