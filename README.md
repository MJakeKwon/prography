# prography-11th-backend

프로그라피 11기 백엔드 과제 - **출결 관리 시스템**입니다.  
기수 기반 IT 동아리의 회원/일정/QR/출결/보증금 관리를 위한 API를 Spring Boot로 구현했습니다.

---

## 1. 프로젝트 개요

본 프로젝트는 프로그라피 세션 출결 관리를 위한 백엔드 시스템입니다.

### 주요 기능
- 회원 등록/조회/수정/탈퇴 (Soft-delete)
- 기수/파트/팀 관리 조회
- 일정 생성/수정/삭제 (Soft-delete)
- QR 생성/갱신
- QR 출석 체크
- 관리자 출결 등록/수정
- 보증금 차감/환급 및 이력 관리
- 출결 요약/목록 조회

---

## 2. 개발 환경

- Java 17
- Spring Boot 3.5.11
- Gradle
- Spring Data JPA
- H2 Database (In-Memory)
- Bean Validation
- BCrypt (`spring-security-crypto`)
- JUnit5

---

## 3. 실행 방법

### 3-1. 서버 실행

```bash
./gradlew bootRun