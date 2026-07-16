package com.commercecore.checkout.service.impl;


import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import com.commercecore.shared.dto.ApiResponse;
import com.commercecore.shared.dto.order.OrderDTO;
import com.commercecore.shared.dto.order.OrderEvent;
import com.commercecore.shared.dto.order.OrderItemDTO;
import com.commercecore.shared.dto.product.ProductDTO;
import com.commercecore.checkout.dto.*;
import com.commercecore.checkout.dto.attribute_value.AttributeValueResponseDto;
import com.commercecore.checkout.dto.product.ProductResponseDto;
import com.commercecore.checkout.dto.product_variant.ProductVariantResponseDto;
import com.commercecore.checkout.entity.Order;
import com.commercecore.checkout.entity.OrderItem;
import com.commercecore.checkout.entity.OrderStatus;
import com.commercecore.checkout.exception.OrderException;
import com.commercecore.checkout.kafka.OrderProducer;
import com.commercecore.checkout.paypal.PayPalService;
import com.commercecore.checkout.redis.OrderRedis;
import com.commercecore.checkout.repository.OrderRepository;
import com.commercecore.checkout.service.OrderService;
import com.commercecore.checkout.service.PaymentAPIClient;
import com.commercecore.checkout.service.ProductAPIClient;

import com.commercecore.checkout.service.state.OrderContext;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderProducer orderProducer;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ProductAPIClient productAPIClient;
    private final PaymentAPIClient paymentAPIClient;
    private final PayPalService payPalService;
    private final OrderRedis orderRedis;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderRequestDto orderRequestDto, Long userId, String email) {
        try {
            if(orderRequestDto.getPaymentMethod().equals("Paypal") && orderRequestDto.getOrderId() == null){
                throw new OrderException("Please provide Paypal's ID", HttpStatus.BAD_REQUEST);
            }

            OrderDTO newOrder = createOrderDTO(orderRequestDto, userId);
            validateStockAndPrice(orderRequestDto, newOrder);

            Order createdOrder = saveOrder(newOrder, orderRequestDto);
            orderRedis.save(modelMapper.map(createdOrder, OrderDTO.class));


            sendOrderEvent(createdOrder, orderRequestDto.getPaymentMethod(), email);

            return modelMapper.map(createdOrder, OrderDTO.class);
        } catch (OrderException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Failed to create order: {}", e.getMessage(), e);
            throw new OrderException("Failed to create order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public OrderResponseDto checkOrderStatusByOrderId(String orderId) {
        // Kiểm tra từ Redis cache
        OrderDTO cachedOrder = orderRedis.findByOrderId(orderId);
        OrderDTO order;

        if (cachedOrder != null) {
            order = cachedOrder;
            LOGGER.info("Order retrieved from Redis cache.");
        } else {
            // Nếu không tìm thấy trong cache, truy vấn cơ sở dữ liệu và lưu vào cache
            order = modelMapper.map(orderRepository.findById(orderId).orElse(null), OrderDTO.class);
            if (order != null) {
                orderRedis.save(order);
                LOGGER.info("Order retrieved from DB and saved to Redis cache.");
            }
        }

        if (order != null) {
            OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
            PaymentDto paymentDto = paymentAPIClient.getPaymentByOrderId(orderId).getBody().getData();

            OrderResponseDto orderResponseDto = new OrderResponseDto();
            orderResponseDto.setOrderDTO(orderDTO);
            orderResponseDto.setPaymentDto(paymentDto);

            return orderResponseDto;
        }
        return null;
    }


    @Override
    public OrderResponseDto updateOrderStatus(String orderId, int version) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            if (order.getVersion() != version) {
                throw new OptimisticLockException("Version conflict!");
            }

            OrderContext context = new OrderContext(order);
            context.handleStateChange(order);

            Order savedOrder = orderRepository.save(order);
            OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);

            // Cập nhật cache Redis
            orderRedis.save(orderDTO);

            PaymentDto paymentDto = paymentAPIClient.getPaymentByOrderId(orderId).getBody().getData();

            OrderResponseDto orderResponseDto = new OrderResponseDto();
            orderResponseDto.setOrderDTO(orderDTO);
            orderResponseDto.setPaymentDto(paymentDto);
            return orderResponseDto;
        }
        return null;
    }


    //* Pre Authorized: ADMINISTRATOR, EMPLOYEE
    @Override
    public OrderDTO cancelOrder(String orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(OrderStatus.CANCELED.getLabel());
            ApiResponse<PaymentDto> paymentDto = paymentAPIClient.getPaymentByOrderId(orderId).getBody();
            if(paymentDto != null && paymentDto.getData() != null && paymentDto.getData().getStatus().equals("Paypal")){
                try {
                    String captureId = payPalService.getCaptureIdFromOrder(orderId);
                    if(captureId != null){
                        sendRefundOrderEvent(order);
                        payPalService.refundPayment(captureId);
                    }
                }catch(Exception e){
                    throw new OrderException("No captures found for this order. Refund cannot be processed.", HttpStatus.BAD_REQUEST);
                }
            }
            OrderDTO savedOrderDTO = modelMapper.map(orderRepository.save(order), OrderDTO.class);
            orderRedis.save(savedOrderDTO);
            return savedOrderDTO;
        }
        return null;
    }

    @Override
    public List<OrderResponseDtoWithOutOrderItems> getAllOrders(Long userId, int page, int size) {
        Page<Order> orderPage = orderRepository.findByUserId(userId, PageRequest.of(page, size));
        return orderPage.stream().map(order -> {
            // Lấy đơn hàng từ cache nếu có
            OrderDTO cachedOrder = orderRedis.findByOrderId(order.getOrderId());
            OrderWithOutOrderItems orderDTO = modelMapper.map(order, OrderWithOutOrderItems.class);
            if (cachedOrder == null) {
                // Nếu không có trong cache, lưu vào cache
                orderRedis.save(modelMapper.map(order, OrderDTO.class));
            }
            PaymentDto paymentDto = paymentAPIClient.getPaymentByOrderId(order.getOrderId()).getBody().getData();
            return new OrderResponseDtoWithOutOrderItems(orderDTO, paymentDto);
        }).collect(Collectors.toList());
    }


    private OrderEvent createOrderEvent(Order createdOrder, String paymentMethod, String email) {
        OrderDTO createdOrderDto = modelMapper.map(createdOrder, OrderDTO.class);
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderDTO(createdOrderDto);
        orderEvent.setStatus("PENDING");
        orderEvent.setMessage("Order status is in pending state");
        orderEvent.setPaymentMethod(paymentMethod);
        orderEvent.setEmail(email);
        return orderEvent;
    }

    private OrderDTO createOrderDTO(OrderRequestDto orderRequestDto, Long userId) {
        OrderDTO newOrder = modelMapper.map(orderRequestDto, OrderDTO.class);
        newOrder.setUserId(userId);
        return newOrder;
    }


    private void validateStockAndPrice(OrderRequestDto orderRequestDTO, OrderDTO newOrder) {
        Set<String> productIds = extractProductIds(orderRequestDTO.getOrderItems());
        List<ProductResponseDto> products = fetchProducts(productIds);

        if (products.size() != productIds.size()) {
            throw new OrderException("Variants not found for ids: " + productIds, HttpStatus.BAD_REQUEST);
        }

        Map<String, ProductResponseDto> productsMap = products.stream()
                .collect(Collectors.toMap(ProductResponseDto::getId, Function.identity()));

        for (OrderItemDTO orderItem : orderRequestDTO.getOrderItems()) {
            ProductResponseDto product = productsMap.get(orderItem.getProductId());
            if (product == null) {
                throw new OrderException("Not found product!", HttpStatus.NOT_FOUND);
            }

            //* UPDATE PRICE AND VALIDATE STOCK
            updatePriceAndValidateStock(orderItem, product);
        }
    }

    private Set<String> extractProductIds(List<OrderItemDTO> orderItems) {
        return orderItems.stream()
                .map(OrderItemDTO::getProductId)
                .collect(Collectors.toSet());
    }

    private List<ProductResponseDto> fetchProducts(Set<String> productIds) {
        return productAPIClient.getProductsByIds(productIds).getBody().getData();
    }

    private void updatePriceAndValidateStock(OrderItemDTO orderItem, ProductResponseDto product) {
        ProductVariantResponseDto selectedVariant = findMatchingVariant(product.getVariants(), orderItem.getVariantId());
        if (selectedVariant != null) {
            if (selectedVariant.getStockQuantity() < orderItem.getQuantity()) {
                throw new OrderException("Insufficient stock for variantId: " + selectedVariant.getId(), HttpStatus.BAD_REQUEST);
            }
            if(selectedVariant.getPrice() != null){
                orderItem.setPrice(selectedVariant.getPrice());
                return;
            }
            orderItem.setPrice(product.getPrice());
        }
    }

    private ProductVariantResponseDto findMatchingVariant(List<ProductVariantResponseDto> variants, Long variantId) {
        if (variants == null) return null;
        return variants.stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    private Order saveOrder(OrderDTO newOrder, OrderRequestDto orderRequestDto) {
        Order order = modelMapper.map(newOrder, Order.class);
        if ("Paypal".equalsIgnoreCase(orderRequestDto.getPaymentMethod())) {
            BigDecimal amount = BigDecimal.valueOf(0);

            for (OrderItemDTO orderItemDTO : orderRequestDto.getOrderItems()) {
                BigDecimal itemTotal = orderItemDTO.getPrice().multiply(BigDecimal.valueOf(orderItemDTO.getQuantity()));
                amount = amount.add(itemTotal);
            }
            order.setOrderId(orderRequestDto.getOrderId());
        } else {
            order.setOrderId(UUID.randomUUID().toString());
        }
        order.setStatus(OrderStatus.PENDING.getLabel());
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.setOrder(order);
        }
        return orderRepository.save(order);
    }

    private void sendOrderEvent(Order createdOrder, String paymentMethod, String email) {
        OrderEvent orderEvent = createOrderEvent(createdOrder, paymentMethod, email);
        orderProducer.sendMessage(orderEvent);
    }

    // ! This method is deprecated.
//    private OrderResponseDto createOrderResponseDto(Order createdOrder, OrderRequestDto orderRequestDto) {
//        OrderResponseDto orderResponseDto = new OrderResponseDto();
//        PaymentDto paymentDto = paymentAPIClient.getPaymentByOrderId(createdOrder.getOrderId()).getBody().getData();
//        OrderDTO createdOrderDto = modelMapper.map(createdOrder, OrderDTO.class);
//        orderResponseDto.setPaymentDto(paymentDto);
//        orderResponseDto.setOrderDTO(createdOrderDto);
//        LOGGER.info("Order created successfully with ID: {}", createdOrder.getOrderId());
//        return orderResponseDto;
//    }

    private void sendRefundOrderEvent(Order order){
        OrderEvent orderEvent = new OrderEvent();
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderEvent.setOrderDTO(orderDTO);
        orderEvent.setMessage("REFUND");
        orderProducer.sendMessage(orderEvent);
    }

}
