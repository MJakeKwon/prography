# 도메인 설계 및 Aggregate 경계

## 1. 설계 방향

과제 요구사항은 출결/보증금 정책이 명확하며, 데이터 정합성이 중요합니다.  
따라서 DDD 느낌을 유지하되, 과제 안정성과 구현 속도를 우선하는 방향으로 설계했습니다.

핵심 원칙
- 도메인별 패키지 분리
- DB 무결성 우선
- 서비스 레이어에서 정책 검증
- 과도한 추상화 지양

---

## 2. 주요 도메인 모델

## Member
회원 계정/기본정보/상태를 관리합니다.

주요 역할
- 회원 정보 보유
- 회원 탈퇴(Soft-delete → `WITHDRAWN`)

---

## Cohort / Part / Team
기수 및 기수 소속 파트/팀 정보입니다.

- `Cohort`: 10기, 11기
- `Part`: 기수별 SERVER, WEB, iOS, ANDROID, DESIGN
- `Team`: 11기 Team A/B/C

설계 포인트
- `Part`, `Team`을 ENUM이 아닌 테이블로 유지
- 기수별 관리 및 확장성 고려

---

## CohortMember
특정 회원의 특정 기수 소속 정보입니다.

주요 책임
- 기수 소속(파트/팀)
- 보증금 잔액 관리
- 공결 횟수 관리

설계 이유
- 보증금/공결 정책이 **회원 전체가 아니라 기수 기준**이기 때문

---

## Session
기수별 정기 모임 일정입니다.

주요 책임
- 일정 생성/수정/삭제(Soft-delete)
- 상태 관리 (`SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`)

---

## QrCode
출석 체크용 QR 정보입니다.

주요 책임
- QR 생성
- QR 갱신
- 만료/활성 상태 판단

---

## Attendance
회원의 특정 세션 출결 정보입니다.

주요 책임
- 출결 상태 저장 (`PRESENT`, `LATE`, `ABSENT`, `EXCUSED`)
- 지각 분/패널티 금액 저장
- 출결 수정 시 상태 변경

---

## DepositHistory
보증금 변경 이력을 관리합니다.

주요 책임
- 초기 설정 기록 (`INITIAL`)
- 패널티 차감 기록 (`PENALTY`)
- 환급 기록 (`REFUND`)

---

## 3. Aggregate 경계 (실용적 기준)

### 1) Member Aggregate
- Root: `Member`
- 책임: 회원 기본 정보 / 상태(탈퇴)

### 2) Cohort Membership Aggregate
- Root: `CohortMember`
- 책임: 기수 소속 / 보증금 / 공결 횟수

### 3) Session Aggregate
- Root: `Session`
- QR 정책은 Session 중심으로 관리 (구현상 `QrCode` 리포지토리 분리 가능)

### 4) Attendance Aggregate
- Root: `Attendance`
- 출결 상태/패널티 저장
- `CohortMember`(보증금/공결)와 협력

---

## 4. 서비스 책임 분리

### AttendanceCommandService
- QR 검증 순서 적용
- 출석 판정(PRESENT/LATE)
- 출결 등록/수정
- 보증금 차감/환급 트리거
- 공결 횟수 증감 처리

### Session / QrCode Service
- 일정 생성 시 QR 자동 생성
- QR 갱신 시 기존 QR 만료 후 새 QR 생성

### Member Service
- 회원 등록/수정/탈퇴
- 회원 등록 시 CohortMember/초기 보증금 이력 생성

---

## 5. 트레이드오프

선택한 방향
- 명시적인 서비스 로직
- DB 제약조건 적극 활용
- 과제 안정성 우선

보류한 것
- 도메인 이벤트 기반 분리
- CQRS 분리
- 고급 동시성 제어 설계

이유
- 과제 범위/시간 내 구현 및 검증 완료가 우선이기 때문