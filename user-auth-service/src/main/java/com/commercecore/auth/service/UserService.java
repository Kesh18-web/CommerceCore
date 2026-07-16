package com.commercecore.auth.service;

import com.commercecore.auth.dto.UserDto;

public interface UserService {
    UserDto getUserByUsername(String username);
}
