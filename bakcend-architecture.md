---

# 2) `docs/architecture.md`

```md
# 시스템 아키텍처 설계

## 1. 설계 목표

본 과제의 목표는 짧은 시간 안에 요구사항을 충족하면서도 안정적으로 동작하는 출결 관리 백엔드를 구현하는 것입니다.

설계 시 우선순위는 다음과 같습니다.
1. 요구사항 충족
2. 데이터 정합성
3. 예외 처리 일관성
4. 유지보수 가능한 구조

---

## 2. 아키텍처 스타일

Spring Boot 기반 **계층형 아키텍처(Layered Architecture)** 를 사용했습니다.

- **Controller**
  - 요청/응답 처리
  - DTO Validation
- **Service**
  - 비즈니스 로직
  - 트랜잭션 경계
  - 정책 검증
- **Repository**
  - DB 접근 (JPA)
- **Entity / Domain**
  - 상태와 핵심 도메인 규칙

```text
Client (Postman)
  → Controller
  → Service (Transaction / Business Rules)
  → Repository (JPA)
  → H2 Database
