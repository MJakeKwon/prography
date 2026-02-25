# 보증금 및 패널티 정책 설계

## 1. 개요

본 시스템은 기수별 회원(`CohortMember`)의 보증금을 관리하며, 출결 상태에 따라 패널티를 차감하고 모든 변경 이력을 기록합니다.

핵심 목표
- 보증금 잔액 정합성 유지
- 변경 이력 추적 가능성 확보
- 출결 수정 시 차액 정산 자동화

---

## 2. 보증금 관리 단위

보증금은 회원 전체가 아니라 **기수 소속 단위(CohortMember)** 로 관리합니다.

이유
- 공결(EXCUSED) 정책이 기수 기준
- 출결/보증금 정책이 기수 운영 규칙과 직접 연결됨

관리 필드 예시
- `deposit` (현재 잔액)
- `excuseCount` (공결 횟수)

---

## 3. 초기 보증금 정책

회원 등록 시 자동 처리
- 보증금 `100,000원` 설정
- `DepositHistory(INITIAL)` 생성

예시
- `type = INITIAL`
- `amount = 100000`
- `balanceAfter = 100000`

---

## 4. 패널티 계산 규칙

| 출결 상태 | 패널티 |
| --- | --- |
| PRESENT | 0원 |
| ABSENT | 10,000원 |
| LATE | `min(지각분 × 500, 10,000)` |
| EXCUSED | 0원 |

설계 포인트
- 패널티 계산 로직을 별도 컴포넌트(예: `PenaltyCalculator`)로 분리
- 등록/수정 로직에서 동일 정책 재사용 가능

---

## 5. 보증금 차감 정책

패널티 > 0 인 경우 처리 순서
1. 현재 잔액 확인
2. 잔액 부족 시 `DEPOSIT_INSUFFICIENT`
3. 보증금 차감
4. `DepositHistory(PENALTY)` 기록

이력 기록 목적
- 잔액 변화 추적
- 디버깅/검증 용이성 확보
- 출결 수정 이력과 연계 가능

---

## 6. 출결 수정 시 차액 정산 정책

출결 수정 시에는 기존 패널티와 새 패널티의 차이를 계산하여 보증금을 조정합니다.

### 패널티 증가
예: `PRESENT(0)` → `ABSENT(10000)`
- 차액만큼 추가 차감
- `DepositHistory(PENALTY)`

### 패널티 감소
예: `ABSENT(10000)` → `EXCUSED(0)`
- 차액만큼 환급
- `DepositHistory(REFUND)`

### 패널티 동일
예: 기존/변경 패널티 동일
- 보증금 변동 없음
- 이력 생성 없음(또는 정책상 생략)

---

## 7. 공결(EXCUSED) 정책

공결은 기수당 최대 3회까지 허용합니다.

규칙
- 다른 상태 → `EXCUSED` : `excuseCount + 1`
- `EXCUSED` → 다른 상태 : `excuseCount - 1`
- 3회 초과 시 `EXCUSE_LIMIT_EXCEEDED`

`EXCUSED`의 패널티는 0원입니다.

---

## 8. 이력 기록 원칙

모든 보증금 변동은 `DepositHistory`에 기록합니다.

타입
- `INITIAL`
- `PENALTY`
- `REFUND`

장점
- 감사 로그 성격의 추적 가능
- 테스트/검증 시 잔액 계산 근거 확인 가능
- 출결 수정 영향 분석 가능

---

## 9. 트랜잭션 정합성

아래 작업은 하나의 트랜잭션에서 처리합니다.
- Attendance 저장/수정
- CohortMember.deposit 변경
- CohortMember.excuseCount 변경
- DepositHistory 생성

중간에 예외 발생 시 전체 롤백하여 정합성을 유지합니다.