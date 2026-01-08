# 팝업/전시/페어 서비스

## 프로젝트 개요

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
│   ├── facade/              # Facade Layer (트랜잭션 외부 I/O 처리)
│   ├── service/             # Business Layer (트랜잭션 단위)
│   ├── repository/          # Data Access Layer
│   ├── {domain}.java/       # JPA Entity
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
