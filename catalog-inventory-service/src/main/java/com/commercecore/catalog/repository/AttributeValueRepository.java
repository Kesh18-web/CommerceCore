package com.commercecore.catalog.repository;

import com.commercecore.catalog.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    List<AttributeValue> findByProductVariantId(Long productVariantId);
}
