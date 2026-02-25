# 출석 처리 흐름 및 검증 순서

## 1. 개요

본 문서는 QR 출석 체크 및 관리자 출결 처리 흐름을 정리합니다.

목표
- 요구사항 검증 순서 준수
- 출결/보증금/공결 정합성 유지
- 예외 발생 시 부분 반영 방지

---

## 2. QR 출석 체크 검증 순서 (요구사항 반영)

`POST /attendances` 호출 시 아래 순서대로 검증합니다.

1. QR hashValue 유효성 → `QR_INVALID`
2. QR 만료 여부 → `QR_EXPIRED`
3. 세션 상태가 `IN_PROGRESS`인지 → `SESSION_NOT_IN_PROGRESS`
4. 회원 존재 여부 → `MEMBER_NOT_FOUND`
5. 회원 탈퇴 여부 → `MEMBER_WITHDRAWN`
6. 중복 출결 여부 (동일 세션 + 동일 회원) → `ATTENDANCE_ALREADY_CHECKED`
7. 기수 회원 정보 존재 여부 → `COHORT_MEMBER_NOT_FOUND`

검증 순서를 고정한 이유:
- 여러 조건이 동시에 잘못된 경우에도 요구사항에서 지정한 우선 에러를 반환해야 하기 때문입니다.

---

## 3. 검증 통과 후 처리 흐름

검증 통과 후 처리 순서

1. 출석 판정 (`PRESENT` / `LATE`)
2. 패널티 계산
3. Attendance 저장
4. 패널티 발생 시 보증금 차감
5. DepositHistory 기록

---

## 4. 출석 판정 규칙

기준 시각
- `세션 날짜 + 세션 시간`

판정
- 현재 시각이 기준 시각 이전(또는 같음) → `PRESENT`
- 현재 시각이 기준 시각 이후 → `LATE`

지각 분(`lateMinutes`)
- 기준 시각 대비 지연된 분 단위 계산
- 음수는 0 처리

---

## 5. 관리자 출결 등록 / 수정

## 5-1. 관리자 출결 등록 (`POST /admin/attendances`)
- 회원/세션/기수 소속 검증
- 상태 기준 패널티 계산
- EXCUSED인 경우 공결 제한 검증
- Attendance 저장
- 보증금 차감/이력 기록(필요 시)

## 5-2. 관리자 출결 수정 (`PUT /admin/attendances/{id}`)
- 기존 Attendance 조회
- 변경 후 상태 기준 새 패널티 계산
- 공결 횟수 증감 처리
- 패널티 차이 계산
    - 증가 → `PENALTY`
    - 감소 → `REFUND`
    - 동일 → 보증금 변동 없음
- Attendance 수정
- DepositHistory 기록(필요 시)

---

## 6. 트랜잭션 경계

출석 체크/출결 수정은 정합성이 중요하므로 **단일 트랜잭션**으로 처리합니다.

트랜잭션에 포함되는 작업
- Attendance 저장/수정
- CohortMember 보증금 변경
- CohortMember 공결 횟수 변경
- DepositHistory 생성

예외 발생 시 전체 롤백됩니다.

---

## 7. 동시성 고려 (과제 범위 내)

### 중복 출결 방지
- 서비스 레이어 중복 검사
- DB UNIQUE (`session_id`, `member_id`) 이중 방어

### 보증금 차감 경쟁 상황
과제 환경(H2, 단일 실행)에서는 가능성이 낮지만,
실서비스에서는 아래 전략을 고려할 수 있습니다.
- Optimistic Lock (`@Version`)
- Pessimistic Lock
- 재시도 전략

---

## 8. 예외 처리 전략

- 정책 위반은 비즈니스 예외로 처리
- Global Exception Handler에서 공통 응답 포맷으로 변환
- 명시적인 에러 코드 사용으로 디버깅/테스트 용이성 확보