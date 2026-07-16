package com.commercecore.checkout.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.commercecore.checkout.dto.product_variant.ProductVariantResponseDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private int version;
    private List<ProductVariantResponseDto> variants;
}
