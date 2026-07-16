package com.commercecore.checkout.service;


import com.commercecore.shared.dto.order.OrderDTO;
import com.commercecore.checkout.dto.OrderRequestDto;
import com.commercecore.checkout.dto.OrderResponseDto;
import com.commercecore.checkout.dto.OrderResponseDtoWithOutOrderItems;

import java.awt.print.Pageable;
import java.util.List;

public interface OrderService {
    OrderDTO placeOrder(OrderRequestDto order, Long userId, String email);
    OrderResponseDto checkOrderStatusByOrderId(String orderId);
    OrderResponseDto updateOrderStatus(String orderId, int version);
    OrderDTO cancelOrder(String orderId, Long userId);
    List<OrderResponseDtoWithOutOrderItems> getAllOrders(Long userId, int page, int size);
}
