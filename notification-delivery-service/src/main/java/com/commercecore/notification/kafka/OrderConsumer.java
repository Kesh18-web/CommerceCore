package com.commercecore.notification.kafka;


import com.commercecore.shared.dto.order.OrderEvent;
import com.commercecore.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private EmailService emailService;


    @KafkaListener(topics = "${spring.kafka.order-topic.name}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderEvent orderEvent){
        try {
            LOGGER.info(String.format("OrderDTO event received in email service -> %s", orderEvent.toString()));

            emailService.sendOrderConfirmationEmail(orderEvent);

        }catch(Exception e){
            LOGGER.warn(String.format("Error message -> %s", e.getMessage()));
        }
    }
}
