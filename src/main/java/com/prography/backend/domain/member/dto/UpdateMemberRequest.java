package com.prography.backend.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMemberRequest {
    private String name;
    private String phone;
    private Long cohortId;
    private Long partId;
    private Long teamId;
}