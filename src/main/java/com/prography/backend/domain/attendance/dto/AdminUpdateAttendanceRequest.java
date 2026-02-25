package com.prography.backend.domain.attendance.dto;

import com.prography.backend.domain.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AdminUpdateAttendanceRequest {

    @NotNull
    private AttendanceStatus status;

    private String reason;
    private LocalDateTime checkedInAt;
}