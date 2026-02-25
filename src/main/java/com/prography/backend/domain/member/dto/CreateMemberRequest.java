package com.prography.backend.domain.member.dto;

import com.prography.backend.domain.member.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMemberRequest {

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    private Role role; // null이면 서비스에서 MEMBER 처리

    @NotNull
    private Long cohortId;

    @NotNull
    private Long partId;

    private Long teamId;
}