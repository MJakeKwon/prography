package com.prography.backend.domain.member.controller;

import com.prography.backend.domain.attendance.service.AttendanceQueryService;
import com.prography.backend.domain.member.dto.MemberResponse;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.service.MemberAdminService;
import com.prography.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberAdminService memberAdminService;
    private final AttendanceQueryService attendanceQueryService;

    // #2 회원 조회
    @GetMapping("/members/{id}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long id) {
        Member member = memberAdminService.getMember(id);
        return ApiResponse.ok(MemberResponse.from(member));
    }

    // #19 내 출결 요약 (명세 path 기준)
    @GetMapping("/members/{id}/attendance-summary")
    public ApiResponse<AttendanceQueryService.AttendanceSummary> getAttendanceSummary(@PathVariable Long id) {
        return ApiResponse.ok(attendanceQueryService.getAttendanceSummary(id));
    }
}