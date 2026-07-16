package com.commercecore.checkout.service;
import com.commercecore.shared.dto.ApiResponse;
import com.commercecore.checkout.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "USER-AUTH-SERVICE")
public interface AuthenticationAPIClient {
    @GetMapping("api/v1/auth/me")
    ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@RequestHeader(HttpHeaders.COOKIE) String cookie);
}
