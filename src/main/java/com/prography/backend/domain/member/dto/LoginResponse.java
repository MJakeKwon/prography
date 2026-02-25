package com.prography.backend.domain.member.dto;

import com.prography.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private Long memberId;
    private String loginId;
    private String name;
    private String role;

    public static LoginResponse from(Member member) {
        return LoginResponse.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .name(member.getName())
                .role(member.getRole().name())
                .build();
    }
}