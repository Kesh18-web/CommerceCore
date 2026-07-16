package com.commercecore.catalog.dto.category;

import lombok.Data;

@Data
public class UpdateCategoryRequestDto {
    private String id;
    private String name;
    private String parentId; // Có thể null nếu là danh mục gốc
}