package com.commercecore.notification.service;

import com.commercecore.shared.dto.ApiResponse;
import com.commercecore.shared.dto.product.ProductDTO;
import com.commercecore.notification.dto.ProductStockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "CATALOG-INVENTORY-SERVICE")
public interface ProductAPIClient {
    @GetMapping("api/v1/products/{id}")
    ResponseEntity<ApiResponse<ProductStockResponse>> getProductById(@RequestParam("id") String id);
}
