package com.prography.backend.domain.attendance.controller;

import com.prography.backend.domain.attendance.dto.AttendanceResponse;
import com.prography.backend.domain.attendance.dto.CheckInByQrRequest;
import com.prography.backend.domain.attendance.entity.Attendance;
import com.prography.backend.domain.attendance.service.AttendanceCommandService;
import com.prography.backend.domain.attendance.service.AttendanceQueryService;
import com.prography.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AttendanceController {

    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceQueryService attendanceQueryService;

    // #17 QR 출석 체크
    @PostMapping("/attendances")
    public ApiResponse<AttendanceResponse> checkInByQr(@Valid @RequestBody CheckInByQrRequest request) {
        Attendance attendance = attendanceCommandService.checkInByQr(request.getHashValue(), request.getMemberId());
        return ApiResponse.ok(AttendanceResponse.from(attendance));
    }

    // #18 내 출결 기록
    @GetMapping("/attendances")
    public ApiResponse<List<AttendanceResponse>> getMyAttendances(@RequestParam Long memberId) {
        List<AttendanceResponse> result = attendanceQueryService.getMyAttendances(memberId)
                .stream()
                .map(AttendanceResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }
}