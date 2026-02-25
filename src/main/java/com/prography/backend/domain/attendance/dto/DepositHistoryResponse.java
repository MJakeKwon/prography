package com.prography.backend.domain.attendance.dto;

import com.prography.backend.domain.deposit.entity.DepositHistory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DepositHistoryResponse {
    private Long id;
    private Long cohortMemberId;
    private Long attendanceId;
    private String type;
    private Integer amount;
    private Integer balanceAfter;
    private String description;

    public static DepositHistoryResponse from(DepositHistory h) {
        return DepositHistoryResponse.builder()
                .id(h.getId())
                .cohortMemberId(h.getCohortMember().getId())
                .attendanceId(h.getAttendance() != null ? h.getAttendance().getId() : null)
                .type(h.getType().name())
                .amount(h.getAmount())
                .balanceAfter(h.getBalanceAfter())
                .description(h.getDescription())
                .build();
    }
}