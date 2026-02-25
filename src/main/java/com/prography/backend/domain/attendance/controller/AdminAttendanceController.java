package com.prography.backend.domain.attendance.controller;

import com.prography.backend.domain.attendance.dto.AdminCreateAttendanceRequest;
import com.prography.backend.domain.attendance.dto.AdminUpdateAttendanceRequest;
import com.prography.backend.domain.attendance.dto.AttendanceResponse;
import com.prography.backend.domain.attendance.dto.DepositHistoryResponse;
import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.attendance.service.AttendanceCommandService;
import com.prography.backend.domain.attendance.service.AttendanceQueryService;
import com.prography.backend.domain.deposit.entity.DepositHistory;
import com.prography.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAttendanceController {

    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceQueryService attendanceQueryService;

    // #20 출결 등록
    @PostMapping("/attendances")
    public ApiResponse<AttendanceResponse> createAttendance(@Valid @RequestBody AdminCreateAttendanceRequest request) {
        Attendance attendance = attendanceCommandService.adminCreateAttendance(
                request.getSessionId(),
                request.getMemberId(),
                request.getStatus(),
                request.getReason(),
                request.getCheckedInAt()
        );
        return ApiResponse.ok(AttendanceResponse.from(attendance));
    }

    // #21 출결 수정
    @PutMapping("/attendances/{id}")
    public ApiResponse<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateAttendanceRequest request
    ) {
        Attendance attendance = attendanceCommandService.adminUpdateAttendance(
                id,
                request.getStatus(),
                request.getReason(),
                request.getCheckedInAt()
        );
        return ApiResponse.ok(AttendanceResponse.from(attendance));
    }

    // #22 일정별 출결 요약
    @GetMapping("/attendances/sessions/{id}/summary")
    public ApiResponse<AttendanceQueryService.AttendanceSummary> getSessionSummary(@PathVariable Long id) {
        return ApiResponse.ok(attendanceQueryService.getSessionAttendanceSummary(id));
    }

    // #23 회원 출결 상세
    @GetMapping("/attendances/members/{id}")
    public ApiResponse<List<AttendanceResponse>> getMemberAttendances(@PathVariable Long id) {
        List<AttendanceResponse> result = attendanceQueryService.getMemberAttendancesForAdmin(id)
                .stream()
                .map(AttendanceResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // #24 일정별 출결 목록
    @GetMapping("/attendances/sessions/{id}")
    public ApiResponse<List<AttendanceResponse>> getSessionAttendances(@PathVariable Long id) {
        List<AttendanceResponse> result = attendanceQueryService.getAttendancesBySession(id)
                .stream()
                .map(AttendanceResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // #25 보증금 이력
    @GetMapping("/cohort-members/{id}/deposits")
    public ApiResponse<List<DepositHistoryResponse>> getDepositHistories(@PathVariable Long id) {
        List<DepositHistory> list = attendanceQueryService.getDepositHistories(id);
        List<DepositHistoryResponse> result = list.stream()
                .map(DepositHistoryResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }
}