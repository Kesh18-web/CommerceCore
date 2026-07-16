package com.commercecore.checkout.service;

import com.commercecore.shared.dto.ApiResponse;

import com.commercecore.checkout.dto.product.ProductResponseDto;
import com.commercecore.checkout.dto.product_variant.ProductVariantResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(name = "CATALOG-INVENTORY-SERVICE")
public interface ProductAPIClient {
    @GetMapping("/api/v1/products/products")
    ResponseEntity<ApiResponse<List<ProductResponseDto>>> getProductsByIds(@RequestParam("ids") Set<String> productIds);

    @GetMapping("/api/v1/products/variants")
    ResponseEntity<List<ProductVariantResponseDto>> getProductsByVariantIds(@RequestParam("variantIds") Set<Long> variantIds);
}
