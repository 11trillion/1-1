# ----------------------------------------------------
# 1. 빌드 스테이지 (Build Stage) - JAR 파일 생성
# ----------------------------------------------------
# JDK 17을 사용한 빌드 환경 설정
FROM gradle:8.8-jdk17-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .

# settings.gradle에 명시된 모든 모듈 디렉토리 복사
COPY common common
COPY api-gateway api-gateway
COPY eureka-server eureka-server
COPY config-server config-server
COPY user-service user-service
COPY point-service point-service
COPY betting-service betting-service
COPY game-service game-service

# Gradle 빌드 실행
# Context가 모듈 디렉토리이므로, 모듈 이름 없이 bootJar 명령을 실행합니다.
RUN chmod +x ./gradlew \
    && ./gradlew :api-gateway:bootJar -x test

# ----------------------------------------------------
# 2. 최종 실행 스테이지 (Final Run Stage)
# ----------------------------------------------------
# 가벼운 JRE 17 환경 이미지 사용
FROM eclipse-temurin:17-jdk-jammy

# 작업 디렉토리 설정:WQ
WORKDIR /app

# 빌드된 JAR 파일을 복사
ARG JAR_FILE=api-gateway/build/libs/api-gateway-*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# API Gateway의 기본 포트(일반적으로 8080 또는 8000번대) 노출
# 이미지에서 확인된 것처럼 8080 포트를 사용하도록 설정합니다.
EXPOSE 8080

# 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]