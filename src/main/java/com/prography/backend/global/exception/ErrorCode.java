package com.prography.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."),

    // auth/member
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    MEMBER_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER_WITHDRAWN", "탈퇴한 회원입니다."),
    LOGIN_ID_DUPLICATED(HttpStatus.CONFLICT, "LOGIN_ID_DUPLICATED", "이미 사용 중인 loginId 입니다."),

    // cohort/part/team
    COHORT_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_NOT_FOUND", "기수를 찾을 수 없습니다."),
    PART_NOT_FOUND(HttpStatus.NOT_FOUND, "PART_NOT_FOUND", "파트를 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_NOT_FOUND", "팀을 찾을 수 없습니다."),
    COHORT_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "COHORT_MEMBER_NOT_FOUND", "기수 회원 정보를 찾을 수 없습니다."),

    // session/qr
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "일정을 찾을 수 없습니다."),
    SESSION_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "SESSION_NOT_IN_PROGRESS", "진행 중인 일정이 아닙니다."),
    SESSION_CANCELLED_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "SESSION_CANCELLED_CANNOT_UPDATE", "취소된 일정은 수정할 수 없습니다."),
    QR_INVALID(HttpStatus.BAD_REQUEST, "QR_INVALID", "유효하지 않은 QR 입니다."),
    QR_EXPIRED(HttpStatus.BAD_REQUEST, "QR_EXPIRED", "만료된 QR 입니다."),

    // attendance/deposit
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "ATTENDANCE_NOT_FOUND", "출결 정보를 찾을 수 없습니다."),
    ATTENDANCE_ALREADY_CHECKED(HttpStatus.CONFLICT, "ATTENDANCE_ALREADY_CHECKED", "이미 출석 체크되었습니다."),
    DEPOSIT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "DEPOSIT_INSUFFICIENT", "보증금이 부족합니다."),
    EXCUSE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "EXCUSE_LIMIT_EXCEEDED", "공결 가능 횟수를 초과했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}