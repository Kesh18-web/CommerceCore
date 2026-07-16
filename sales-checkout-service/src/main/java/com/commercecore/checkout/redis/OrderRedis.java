package com.commercecore.checkout.redis;

import com.commercecore.shared.dto.order.OrderDTO;
import com.commercecore.checkout.entity.Order;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class OrderRedis {
    private static final String HASH_KEY = "Order";
    @Autowired
    private RedisTemplate redisTemplate;

    public void save(OrderDTO order) {
        try {
            redisTemplate.opsForHash().put(HASH_KEY, order.getOrderId(), order);
            redisTemplate.expire(HASH_KEY, Duration.ofHours(1));

        } catch (Exception e) {
            throw new RuntimeException("Error saving order in Redis: " + e.getMessage(), e);
        }
    }

    public OrderDTO findByOrderId(String id) {
        try {
            return (OrderDTO) redisTemplate.opsForHash().get(HASH_KEY, id);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteByOrderId(String id) {
        try {
            redisTemplate.opsForHash().delete(HASH_KEY, id);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Not Found", e);
        }
    }
}
