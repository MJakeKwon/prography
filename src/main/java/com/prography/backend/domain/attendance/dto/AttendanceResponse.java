package com.prography.backend.domain.attendance.dto;

import com.prography.backend.domain.attendance.entity.Attendance;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AttendanceResponse {
    private Long id;
    private Long sessionId;
    private Long memberId;
    private Long qrcodeId;
    private String status;
    private Integer lateMinutes;
    private Integer penaltyAmount;
    private String reason;
    private LocalDateTime checkedInAt;

    public static AttendanceResponse from(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .sessionId(attendance.getSession().getId())
                .memberId(attendance.getMember().getId())
                .qrcodeId(attendance.getQrcode() != null ? attendance.getQrcode().getId() : null)
                .status(attendance.getStatus().name())
                .lateMinutes(attendance.getLateMinutes())
                .penaltyAmount(attendance.getPenaltyAmount())
                .reason(attendance.getReason())
                .checkedInAt(attendance.getCheckedInAt())
                .build();
    }
}