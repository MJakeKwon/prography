package com.prography.backend.domain.member.controller;

import com.prography.backend.domain.member.dto.LoginRequest;
import com.prography.backend.domain.member.dto.LoginResponse;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.service.AuthService;
import com.prography.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Member member = authService.login(request.getLoginId(), request.getPassword());
        return ApiResponse.ok(LoginResponse.from(member));
    }
}