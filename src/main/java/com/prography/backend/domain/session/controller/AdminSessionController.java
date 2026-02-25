package com.prography.backend.domain.session.controller;

import com.prography.backend.domain.session.dto.CreateSessionRequest;
import com.prography.backend.domain.session.dto.QRCodeResponse;
import com.prography.backend.domain.session.dto.SessionResponse;
import com.prography.backend.domain.session.dto.UpdateSessionRequest;
import com.prography.backend.domain.session.entity.QRCode;
import com.prography.backend.domain.session.entity.Session;
import com.prography.backend.domain.session.service.SessionAdminService;
import com.prography.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminSessionController {

    private final SessionAdminService sessionAdminService;

    // #11 일정 목록 (관리자)
    @GetMapping("/admin/sessions")
    public ApiResponse<List<SessionResponse>> getAdminSessions() {
        List<SessionResponse> result = sessionAdminService.getAdminSessions()
                .stream()
                .map(SessionResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // #12 일정 생성
    @PostMapping("/admin/sessions")
    public ApiResponse<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        Session session = sessionAdminService.createSession(
                request.getTitle(),
                request.getSessionDate(),
                request.getSessionTime(),
                request.getLocation(),
                request.getStatus()
        );
        return ApiResponse.ok(SessionResponse.from(session));
    }

    // #13 일정 수정
    @PutMapping("/admin/sessions/{id}")
    public ApiResponse<SessionResponse> updateSession(
            @PathVariable Long id,
            @RequestBody UpdateSessionRequest request
    ) {
        Session session = sessionAdminService.updateSession(
                id,
                request.getTitle(),
                request.getSessionDate(),
                request.getSessionTime(),
                request.getLocation()
        );
        return ApiResponse.ok(SessionResponse.from(session));
    }

    // #14 일정 삭제
    @DeleteMapping("/admin/sessions/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        sessionAdminService.deleteSession(id);
        return ApiResponse.ok(null);
    }

    // #15 QR 생성
    @PostMapping("/admin/sessions/{id}/qrcodes")
    public ApiResponse<QRCodeResponse> createQrCode(@PathVariable Long id) {
        QRCode qrCode = sessionAdminService.createQRCode(id);
        return ApiResponse.ok(QRCodeResponse.from(qrCode));
    }

    // #16 QR 갱신
    @PutMapping("/admin/qrcodes/{id}")
    public ApiResponse<QRCodeResponse> refreshQrCode(@PathVariable Long id) {
        QRCode qrCode = sessionAdminService.refreshQRCode(id);
        return ApiResponse.ok(QRCodeResponse.from(qrCode));
    }
}