package com.commercecore.checkout.repository;

import com.commercecore.checkout.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;



public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByUserId(@PathVariable("userId") Long userId, Pageable pageable);
}
