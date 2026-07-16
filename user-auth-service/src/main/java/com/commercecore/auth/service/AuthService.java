package com.commercecore.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import com.commercecore.auth.dto.AuthRequest;
import com.commercecore.auth.dto.SignUpRequest;
import com.commercecore.auth.entity.UserCredential;

public interface AuthService {
    String saveUser(SignUpRequest userCredential);
    String generateToken(AuthRequest authRequest, HttpServletResponse response);
    void validateToken(String token);
}
