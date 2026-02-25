package com.prography.backend.domain.member.controller;

import com.prography.backend.domain.member.dto.CreateMemberRequest;
import com.prography.backend.domain.member.dto.MemberResponse;
import com.prography.backend.domain.member.dto.UpdateMemberRequest;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.service.MemberAdminService;
import com.prography.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberAdminService memberAdminService;

    // #3 회원 등록
    @PostMapping
    public ApiResponse<MemberResponse> createMember(@Valid @RequestBody CreateMemberRequest request) {
        Member member = memberAdminService.createMember(
                request.getLoginId(),
                request.getPassword(),
                request.getName(),
                request.getPhone(),
                request.getRole(),
                request.getCohortId(),
                request.getPartId(),
                request.getTeamId()
        );
        return ApiResponse.ok(MemberResponse.from(member));
    }

    // #4 회원 대시보드
    @GetMapping
    public ApiResponse<List<MemberResponse>> getMemberDashboard() {
        List<MemberResponse> result = memberAdminService.getMemberDashboard()
                .stream()
                .map(MemberResponse::from)
                .toList();
        return ApiResponse.ok(result);
    }

    // #5 회원 상세
    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> getMemberDetail(@PathVariable Long id) {
        return ApiResponse.ok(MemberResponse.from(memberAdminService.getAdminMemberDetail(id)));
    }

    // #6 회원 수정
    @PutMapping("/{id}")
    public ApiResponse<MemberResponse> updateMember(
            @PathVariable Long id,
            @RequestBody UpdateMemberRequest request
    ) {
        Member member = memberAdminService.updateMember(
                id,
                request.getName(),
                request.getPhone(),
                request.getCohortId(),
                request.getPartId(),
                request.getTeamId()
        );
        return ApiResponse.ok(MemberResponse.from(member));
    }

    // #7 회원 탈퇴
    @DeleteMapping("/{id}")
    public ApiResponse<Void> withdrawMember(@PathVariable Long id) {
        memberAdminService.withdrawMember(id);
        return ApiResponse.ok(null);
    }
}