package com.esunbank.library.presentation.controller;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esunbank.library.business.service.AuthService;
import com.esunbank.library.common.response.ApiResponse;
import com.esunbank.library.presentation.dto.ApiDtoMapper;
import com.esunbank.library.presentation.dto.AuthDtos;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ApiDtoMapper mapper;

    public AuthController(AuthService authService, ApiDtoMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.UserResponse>> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request
    ) {
        var user = authService.register(request.phoneNumber(), request.password(), request.userName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(mapper.toUserResponse(user)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.LoginResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        var result = authService.login(
                request.phoneNumber(),
                request.password(),
                httpRequest.getRemoteAddr()
        );
        return ApiResponse.of(mapper.toLoginResponse(result));
    }
}
