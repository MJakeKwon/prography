
---

# 3) `docs/domain-design.md`

```md
# 도메인 설계 및 Aggregate 경계

## 1. 설계 방향

과제 요구사항은 비즈니스 규칙이 명확하고, 출결/보증금 정합성이 중요합니다.  
따라서 다음 기준으로 설계했습니다.

- DDD 느낌의 도메인 분리
- 과도한 추상화보다 과제 안정성 우선
- Aggregate 경계는 트랜잭션 정합성 기준으로 설정
- DB 무결성과 서비스 검증을 함께 사용

---

## 2. 핵심 도메인 모델

### Member
회원의 계정/신상/상태를 관리합니다.

주요 속성:
- `loginId`
- `passwordHash`
- `name`
- `phone`
- `role`
- `status` (`ACTIVE`, `WITHDRAWN`)

역할:
- 회원 탈퇴(Soft-delete 상태 변경)

---

### Cohort / Part / Team
기수 및 기수 소속 파트/팀을 관리합니다.

- `Cohort`: 10기/11기
- `Part`: 기수별 SERVER, WEB, iOS, ANDROID, DESIGN
- `Team`: 11기 Team A/B/C

설계 포인트:
- `Part`, `Team`은 ENUM이 아닌 테이블로 유지
- 기수별 관리 및 확장성 고려

---

### CohortMember
특정 회원의 특정 기수 소속 정보입니다.

주요 속성:
- `cohort`
- `member`
- `part`
- `team`
- `deposit`
- `excuseCount`

역할:
- 기수별 보증금 잔액 관리
- 기수별 공결 횟수 관리

> 보증금/공결은 회원 전체가 아니라 **기수 단위 정책**이므로 `CohortMember`에 위치시켰습니다.

---

### Session
기수별 정기 모임 일정입니다.

주요 속성:
- `cohort`
- `title`
- `sessionDate`
- `sessionTime`
- `location`
- `status` (`SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)

역할:
- 일정 생성/수정/삭제(Soft-delete)

---

### QrCode
출석 체크용 QR 정보입니다.

주요 속성:
- `session`
- `hashValue` (UUID 기반)
- `expiresAt`
- `revokedAt`

역할:
- QR 생성/갱신
- 활성/만료 상태 판단

---

### Attendance
회원의 특정 세션 출결 정보입니다.

주요 속성:
- `session`
- `member`
- `qrcode`
- `status` (`PRESENT`, `LATE`, `ABSENT`, `EXCUSED`)
- `lateMinutes`
- `penaltyAmount`
- `reason`
- `checkedInAt`

역할:
- 출결 상태/패널티 기록
- 출결 수정 시 상태 변경

---

### DepositHistory
보증금 변경 이력입니다.

주요 속성:
- `cohortMember`
- `attendance` (nullable)
- `type` (`INITIAL`, `PENALTY`, `REFUND`)
- `amount`
- `balanceAfter`
- `description`

역할:
- 보증금 변경 이력 추적
- 초기 지급/차감/환급 기록

---

## 3. Aggregate 경계 정리

## Aggregate 1) Member Aggregate
루트:
- `Member`

포함 책임:
- 회원 기본 정보 관리
- 회원 상태 변경 (탈퇴)

비고:
- `CohortMember`는 별도 Aggregate로 분리 (기수별 정책 때문)

---

## Aggregate 2) Cohort Membership Aggregate
루트:
- `CohortMember`

연관:
- `Member`, `Cohort`, `Part`, `Team` 참조

포함 책임:
- 기수 소속 정보
- 보증금 잔액
- 공결 횟수

핵심 이유:
- 보증금과 공결은 출결 도메인과 강하게 연결되지만, 상태 저장 위치는 `CohortMember`가 자연스러움

---

## Aggregate 3) Session Aggregate
루트:
- `Session`

연관:
- `QrCode` (세션에 종속적으로 운영)

포함 책임:
- 일정 상태 관리
- QR 생성/갱신 정책 적용

비고:
- 구현 편의상 `QrCode`를 별도 리포지토리로 관리하되, 정책적으로는 Session 중심으로 다룸

---

## Aggregate 4) Attendance Aggregate
루트:
- `Attendance`

연관:
- `Session`, `Member`, `QrCode` 참조
- `CohortMember` 상태(보증금/공결)와 협력

포함 책임:
- 출석 체크/출결 등록/출결 수정
- 출결 상태에 따른 패널티 계산 결과 저장

비고:
- 보증금 변경 이력(`DepositHistory`)은 출결 처리 과정의 결과물로 생성됨

---

## 4. 서비스 책임 분리 원칙

### AttendanceCommandService
- QR 검증 순서 적용
- 출석 판정(PRESENT/LATE)
- Attendance 생성/수정
- 보증금 차감/환급 트리거
- 공결 횟수 증감 처리

### Deposit 관련 서비스/정책 컴포넌트
- 패널티 계산
- 보증금 차감/환급
- 이력 생성

### Session/QrCode 서비스
- 일정 생성 시 QR 자동 생성
- QR 갱신 시 이전 QR 만료 처리

---![ERD.PNG](../../../Desktop/ERD.PNG)

## 5. 설계상 트레이드오프

### 선택한 방향
- 과제 안정성 중심
- 명시적 서비스 로직
- DB 무결성 적극 사용

### 보류한 것
- 도메인 이벤트 분리
- 복잡한 Aggregate 간 이벤트 기반 협력
- 고급 CQRS 분리

이유:
- 과제 범위와 시간 내 안정적인 구현/검증이 우선이기 때문