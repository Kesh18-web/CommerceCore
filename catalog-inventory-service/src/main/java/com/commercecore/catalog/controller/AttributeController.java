package com.commercecore.catalog.controller;

import lombok.RequiredArgsConstructor;
import com.commercecore.shared.dto.ApiResponse;
import com.commercecore.catalog.dto.attribute.UpdateAttributeRequestDto;
import com.commercecore.catalog.entity.Attribute;
import com.commercecore.catalog.service.AttributeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes")
@RequiredArgsConstructor
public class AttributeController {


    private final AttributeService attributeService;

    // --- Attribute APIs ---

    /**
     * Thêm Attribute mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAttribute(
            @RequestBody UpdateAttributeRequestDto requestDto) {
        Attribute createdAttribute = attributeService.createAttribute(requestDto.getName(), requestDto.getDataType());
        return new ResponseEntity<>(new ApiResponse<>(createdAttribute, HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    /**
     * Cập nhật Attribute hiện có
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateAttribute(
            @PathVariable Long id,
            @RequestBody UpdateAttributeRequestDto requestDto) {
        Attribute updatedAttribute = attributeService.updateAttribute(id, requestDto.getName(), requestDto.getDataType());
        return new ResponseEntity<>(new ApiResponse<>(updatedAttribute, HttpStatus.OK.value()), HttpStatus.OK);
    }

    /**
     * Xóa Attribute
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy tất cả Attributes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllAttributes() {
        List<Attribute> attributes = attributeService.getAllAttributes();
        return new ResponseEntity<>(new ApiResponse<>(attributes, HttpStatus.OK.value()), HttpStatus.OK);
    }
}
