package com.commercecore.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.commercecore.auth.enums.ERole;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    private ERole authority;
}