# 🏛️ 시스템 아키텍처 설계 (Architecture Design)

## 1. 🎯 설계 목표
본 프로젝트는 제한된 시간 내에 요구사항을 완벽히 충족하면서도, 실제 운영 환경에서 발생할 수 있는 데이터 정합성 문제를 방지하는 데 초점을 맞췄습니다.

- **✅ 요구사항 충족:** 필수 API 16개 및 가산점 API 9개의 완벽한 구현
- **⚖️ 데이터 정합성:** 출결 상태 변화에 따른 보증금의 정확한 자동 정산
- **🛡️ 예외 처리 일관성:** 사용자 및 관리자에게 명확한 에러 코드 피드백 제공
- **🧩 유지보수성:** 도메인 간 결합도를 낮춘 도메인 주도 패키지 설계

---

## 2. 🏗️ 아키텍처 스타일
Spring Boot 기반의 **계층형 아키텍처(Layered Architecture)**를 채택하여 각 레이어의 책임을 명확히 분리했습니다.

- **🎮 Controller (Presentation Layer):** 요청/응답 처리 및 DTO 유효성 검증(`@Valid`) 수행
- **⚙️ Service (Business Layer):** 트랜잭션(`@Transactional`) 경계 설정 및 핵심 비즈니스 로직 처리
- **🗄️ Repository (Data Access Layer):** Spring Data JPA를 사용한 데이터베이스 영속성 계층
- **💎 Entity (Domain Layer):** DB 테이블 매핑 및 핵심 도메인 규칙 내포

```text
Client (Postman) ➜ Controller ➜ Service (Transaction) ➜ Repository (JPA) ➜ H2 Database
```

---

## 3. 📂 패키지 구조 전략
코드의 응집도를 높이고 도메인별 확장이 용이하도록 **도메인 중심의 패키지 구조**를 사용했습니다.

- `domain.member`: 회원 정보 및 인증 관련
- `domain.cohort`: 기수, 파트, 팀 관리
- `domain.session`: 세션(일정) 및 QR 코드 관리
- `domain.attendance`: QR 출석 및 관리자 출결 수정
- `domain.deposit`: 보증금 변동 및 이력 관리
- `global.*`: 공통 예외 처리, 설정, 공통 응답 포맷

---

## 4. 🔍 데이터 정합성 및 검증 전략
서비스의 안정성을 위해 **DB 제약 조건(Hard-guard)**과 **서비스 레이어 검증(Soft-guard)**을 병행했습니다.

### 주요 DB 제약 조건
- `members.login_id`: `UNIQUE` (중복 가입 방지)
- `attendances(session_id, member_id)`: `UNIQUE` (중복 출석 방지)
- `cohort_members(cohort_id, member_id)`: `UNIQUE` (기수 내 중복 배정 방지)
- `qrcodes.hash_value`: `UNIQUE` (QR 고유성 보장)

### 서비스 레이어 핵심 검증
- **👤 회원:** 탈퇴 여부(`WITHDRAWN`) 상시 체크
- **📅 세션:** `CANCELLED` 상태 시 수정 불가 및 회원 노출 제외
- **📱 QR:** 생성 후 24시간 경과 여부 및 활성 QR 존재 여부 검증
- **💰 보증금:** 패널티 차감 시 잔액 부족 여부 검증
- **🚫 공결:** 기수당 최대 3회 초과 여부 확인

---

## 5. 🔄 트랜잭션(Transaction) 관리
데이터의 원자성을 보장하기 위해 다중 엔티티 수정이 일어나는 핵심 로직은 하나의 트랜잭션으로 처리합니다.

- **🆕 회원 등록:** 회원 생성 + 기수 배정 + 초기 보증금 설정 + 이력 생성이 통합 처리됩니다.
- **✅ QR 출석 체크:** 출결 기록 저장 + 패널티 계산 + 보증금 차감이 일관되게 처리됩니다.
- **📝 출결 수정:** 패널티 차액 계산 + 보증금 환급/추가 차감 + 공결 횟수 조정이 모두 반영되거나 롤백됩니다.

---

## 6. ⚠️ 예외 처리 전략
사용자에게 친절하고 일관된 응답을 제공하기 위해 **Global Exception Handler**를 구축했습니다.

- **🛑 비즈니스 예외:** 정책 위반 시 명시적인 에러 코드(`ErrorCode`)와 메시지를 포함한 커스텀 예외를 발생시킵니다.
- **📦 공통 포맷:** 모든 에러 응답은 일관된 JSON 포맷으로 반환되어 프론트엔드 처리가 용이합니다.
    - *주요 코드:* `QR_EXPIRED`, `ATTENDANCE_ALREADY_CHECKED`, `DEPOSIT_INSUFFICIENT`, `EXCUSE_LIMIT_EXCEEDED`

---

## 7. 🚀 향후 개선 포인트 (Ideal Architecture)
본 과제의 범위를 넘어, 실제 서비스 운영 시 도입 가능한 이상적인 개선 사항입니다.

- **🔐 인증/인가 추가:** Spring Security와 JWT를 도입하여 관리자와 회원의 접근 권한 세분화
- **⚡ Redis 기반 QR 관리:** QR 코드의 24시간 만료 및 단일 활성 정책을 Redis의 TTL(Time-To-Live) 기능을 통해 구현하여 DB 부하를 줄이고 성능 최적화
- **🔍 조회 최적화:** QueryDSL을 도입하여 동적 쿼리 처리 및 대용량 데이터 조회 성능 개선
- **🔒 동시성 제어:** 다수의 유저가 동시에 출석 체크를 시도할 경우를 대비한 분산 락(Distributed Lock) 등 고도화된 제어 전략 도입