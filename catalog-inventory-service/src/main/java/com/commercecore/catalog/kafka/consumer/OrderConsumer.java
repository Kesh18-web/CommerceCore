package com.commercecore.catalog.kafka.consumer;

import lombok.RequiredArgsConstructor;
import com.commercecore.shared.dto.order.OrderDTO;
import com.commercecore.shared.dto.order.OrderEvent;
import com.commercecore.shared.dto.order.OrderItemDTO;
import com.commercecore.catalog.entity.ProductVariant;
import com.commercecore.catalog.exception.InsufficientStockException;
import com.commercecore.catalog.service.ProductVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    private final ProductVariantService productVariantService;

    @KafkaListener(topics = "${spring.kafka.create-order-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(OrderEvent orderEvent) {
        LOGGER.info("Received OrderEvent: {}", orderEvent);

        OrderDTO orderDTO = orderEvent.getOrderDTO();
        Set<Long> variantIds = orderDTO.getOrderItems().stream()
                .map(OrderItemDTO::getVariantId)
                .collect(Collectors.toSet());

        // Lấy danh sách các ProductVariant theo variantIds
        Map<Long, ProductVariant> variantMap = productVariantService.getProductVariantByIds(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, variant -> variant));

        // Cập nhật tồn kho sản phẩm
        for (OrderItemDTO orderItem : orderDTO.getOrderItems()) {
            updateStockForVariant(orderItem, variantMap);
        }

        LOGGER.info("Successfully processed OrderEvent for orderId: {}", orderDTO.getOrderId());
    }

    private void updateStockForVariant(OrderItemDTO orderItem, Map<Long, ProductVariant> variantMap) {
        Long variantId = orderItem.getVariantId();
        ProductVariant productVariant = variantMap.get(variantId);

        if (productVariant == null) {
            LOGGER.error("ProductVariant with id {} not found.", variantId);
            throw new RuntimeException("ProductVariant with id " + variantId + " not found.");
        }

        int requiredQuantity = orderItem.getQuantity();
        int currentStock = productVariant.getStockQuantity();
        int updatedStock = currentStock - requiredQuantity;

        if (updatedStock < 0) {
            LOGGER.error("Insufficient stock for variant {}. Required: {}, Available: {}", variantId, requiredQuantity, currentStock);
            throw new InsufficientStockException("Insufficient stock for variant " + variantId);
        }

        productVariant.setStockQuantity(updatedStock);
        productVariantService.saveProductVariant(productVariant);

        LOGGER.info("Updated stock for variant {}. New stock: {}", variantId, updatedStock);
    }
}
