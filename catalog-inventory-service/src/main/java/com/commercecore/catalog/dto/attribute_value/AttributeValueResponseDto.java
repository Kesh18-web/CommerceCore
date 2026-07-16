package com.commercecore.catalog.dto.attribute_value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.commercecore.catalog.dto.attribute.AttributeResponseDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeValueResponseDto {
    private AttributeResponseDto attribute;
    private String value;
}
