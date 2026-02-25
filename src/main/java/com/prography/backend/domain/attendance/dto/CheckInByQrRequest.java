package com.prography.backend.domain.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckInByQrRequest {

    @NotBlank
    private String hashValue;

    @NotNull
    private Long memberId;
}