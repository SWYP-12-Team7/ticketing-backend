# Build stage
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Gradle wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .

# 모듈별 build.gradle.kts 복사
COPY apps/ticketing-api/build.gradle.kts apps/ticketing-api/
COPY modules/jpa/build.gradle.kts modules/jpa/

# 의존성 다운로드 (캐싱 활용)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY apps apps
COPY modules modules

# 빌드 (테스트 제외)
RUN ./gradlew :apps:ticketing-api:bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=builder /app/apps/ticketing-api/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
