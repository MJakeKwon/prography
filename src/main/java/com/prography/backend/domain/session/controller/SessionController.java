package com.prography.backend.domain.session.controller;

import com.prography.backend.domain.session.dto.SessionResponse;
import com.prography.backend.domain.session.service.SessionAdminService;
import com.prography.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class SessionController {

    private final SessionAdminService sessionAdminService;

    // #10 일정 목록 (회원)
    @GetMapping
    public ApiResponse<List<SessionResponse>> getSessions() {
        List<SessionResponse> result = sessionAdminService.getMemberSessions()
                .stream()
                .map(SessionResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }
}