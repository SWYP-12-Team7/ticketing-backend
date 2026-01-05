# AGENTS.md

이 문서는 AI 에이전트가 이 프로젝트에서 작업할 때 참고하는 가이드라인입니다.

## 프로젝트 개요

- **프로젝트명**: example
- **프레임워크**: Spring Boot 4.0.1
- **언어**: Java 25
- **빌드 도구**: Gradle (Kotlin DSL)
- **데이터베이스**: MySQL + Flyway 마이그레이션

## 기술 스택

- Spring Boot WebMVC
- Spring Data JPA
- Spring Security
- Spring RestClient
- Flyway (MySQL)
- Lombok
- Testcontainers (테스트용)

## 프로젝트 구조

### 멀티모듈 의존성
```
apps/ticketing-api (서버 실행) → services → modules/jpa
```
- **apps**: 실행 가능한 애플리케이션 (BootJar)
- **services**: 비즈니스 로직
- **modules**: 공통 라이브러리 (jpa, querydsl, flyway 등)

### 패키지 구조
```
src/main/java/com/services
├── {domain}/                # 도메인별 패키지
│   ├── controller/          # Presentation Layer
│   ├── facade/              # Facade Layer (트랜잭션 외부 I/O 처리)
│   ├── service/             # Business Layer (트랜잭션 단위)
│   ├── repository/          # Data Access Layer
│   ├── entity/              # JPA Entity
│   └── dto/                 # DTO 클래스
└── common/
    └── exception/           # CustomException
```

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 애플리케이션 실행
./gradlew bootRun
```

## 아키텍처 원칙

1. **Layered Architecture** 지향
2. **패키지 구조**: 도메인별 패키지 하위에 레이어 배치
3. **DIP 적용**: 외부 의존성(Repository 등)은 DIP 적용
   - 단, `JpaRepository`처럼 interface 기반은 DIP 불필요
4. **Entity**: JPA Entity를 그대로 사용
5. **트랜잭션**: Service 레이어가 단위 트랜잭션
   - 트랜잭션과 무관한 I/O 작업은 Facade를 통해 트랜잭션 외부에서 처리
6. **비동기 처리**: History성 데이터는 async 이벤트 발행으로 비동기 처리
7. **FK 사용**: 엔티티 관계 필요 시 FK 적용

## 코딩 컨벤션

### 일반 규칙
- Lombok을 사용하여 보일러플레이트 코드 최소화
- 들여쓰기: 2 spaces
- 클래스명: PascalCase
- 메서드/변수명: camelCase
- 상수: UPPER_SNAKE_CASE

### DTO 규칙
- **파라미터 3개 이상**일 때 DTO 사용
- DTO는 가능하면 **record**로 생성

### 메서드 규칙
- 리턴값이 nullable하면 **Optional**로 감싸기
- 엔티티만으로 비즈니스 로직 작성 가능 시, 엔티티 클래스 내부에 메서드로 작성하여 응집도 향상

### 유틸리티
- 문자열 비어있는지 검증: `StringUtils.hasText()` 사용

### 예외 처리
- 프로젝트에 정의된 **CustomException** 사용

## Naming Convention

| 구분 | 접미사 | 예시 |
|------|--------|------|
| Controller Request DTO | `Request` | `CreateUserRequest` |
| Controller Response DTO | `Response` | `UserResponse` |
| Facade/Service Input DTO | `Query`, `Command` | `CreateUserCommand` |
| Facade/Service Output DTO | `Result` | `UserResult` |
| List 변수 | `-s`, `-es` | `users`, `companies` |

- Service 메서드 네이밍은 명확하게: `UserService.createUser()`

## 데이터베이스 마이그레이션

- Flyway를 사용하여 마이그레이션 관리
- 마이그레이션 파일 위치: `src/main/resources/db/migration/`
- 파일명 규칙: `V{버전}__{설명}.sql` (예: `V1__create_users_table.sql`)

## 테스트 가이드라인

### 테스트 전략
- **단위 테스트**: 최대한 많은 검증 수행
- **통합 테스트**: Happy-Case 위주로 작성
- **동시성 테스트**: 공유자원 동시성 이슈 있는 코드는 반드시 작성

### 테스트 작성 방식
- **given-when-then** 기법 사용
- JUnit 5 + Spring Boot Test
- Testcontainers로 MySQL 컨테이너를 사용한 통합 테스트

## 주의사항

- 새로운 엔티티 추가 시 반드시 Flyway 마이그레이션 스크립트 작성
- Security 설정 변경 시 기존 인증/인가 로직에 영향 없는지 확인
- RestClient 사용 시 적절한 에러 핸들링 구현
