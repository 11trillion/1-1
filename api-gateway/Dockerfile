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

# 모든 모듈의 build.gradle 파일 복사 (캐시 키)
COPY common/build.gradle common/
COPY config-server/build.gradle config-server/
COPY eureka-server/build.gradle eureka-server/
COPY betting-service/build.gradle betting-service/
COPY user-service/build.gradle user-service/
COPY game-service/build.gradle game-service/
COPY point-service/build.gradle point-service/
COPY api-gateway/build.gradle api-gateway/

# Gradle 빌드 실행
RUN chmod +x ./gradlew \
    && ./gradlew :api-gateway:classes -x test --no-daemon

# 소스 코드 복사 (변경 잦은 레이어)
COPY common/src common/src
COPY config-server/src config-server/src
COPY eureka-server/src eureka-server/src
COPY betting-service/src betting-service/src
COPY user-service/src user-service/src
COPY game-service/src game-service/src
COPY point-service/src point-service/src
COPY api-gateway/src api-gateway/src

# Gradle 빌드 및 JAR 파일 이름 통일
RUN ./gradlew :api-gateway:bootJar -x test --no-daemon

# ----------------------------------------------------
# 2. 최종 실행 스테이지 (Final Run Stage)
# ----------------------------------------------------
# 가벼운 JRE 17 환경 이미지 사용
FROM eclipse-temurin:17-jdk-jammy

# 작업 디렉토리 설정:WQ
WORKDIR /app

# 빌드된 JAR 파일을 복사
COPY --from=build /app/api-gateway/build/libs/api-gateway-*.jar app.jar

# API Gateway의 기본 포트(일반적으로 8080 또는 8000번대) 노출
# 이미지에서 확인된 것처럼 8080 포트를 사용하도록 설정합니다.
EXPOSE 8080

# 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]