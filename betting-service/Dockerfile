# ----------------------------------------------------
# 1. 빌드 스테이지 (Build Stage)
# ----------------------------------------------------
# eclipse-temurin 이미지 사용 (가장 안정적인 대안)
FROM gradle:8.8-jdk17-alpine AS build

# 작업 디렉토리를 /app으로 설정
WORKDIR /app

# 1. 필수 Gradle 루트 파일 복사 및 권한 부여
# gradlew 실행, Wrapper 설정, 루트 설정 파일들을 모두 복사합니다.
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle ./gradle

# 2. 모든 모듈의 build.gradle 파일 복사
COPY common/build.gradle common/
COPY api-gateway/build.gradle api-gateway/
COPY eureka-server/build.gradle eureka-server/
COPY config-server/build.gradle config-server/
COPY user-service/build.gradle user-service/
COPY point-service/build.gradle point-service/
COPY betting-service/build.gradle betting-service/
COPY game-service/build.gradle game-service/

# 3. 종속성 다운로드 및 캐시 생성
RUN ./gradlew dependencies

# 4. 소스 코드 복사 (핵심 최적화: 'src' 디렉토리만 복사)
COPY common/src common/src
COPY api-gateway/src api-gateway/src
COPY eureka-server/src eureka-server/src
COPY config-server/src config-server/src
COPY user-service/src user-service/src
COPY point-service/src point-service/src
COPY betting-service/src betting-service/src
COPY game-service/src game-service/src

# Gradle 빌드 실행
# Context가 모듈 디렉토리이므로, 모듈 이름 없이 bootJar 명령을 실행합니다.
RUN chmod +x ./gradlew \
    && ./gradlew :betting-service:bootJar -x test

# ----------------------------------------------------
# 2. 최종 실행 스테이지 (Final Run Stage)
# ----------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy

# 작업 디렉토리를 /app으로 설정
WORKDIR /app

# 빌드된 JAR 파일을 최종 이미지로 복사
# Context 내에서 빌드되므로 경로는 build/libs/ 입니다.
ARG JAR_FILE=betting-service/build/libs/betting-service-*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# 포트 노출
EXPOSE 8080

# 컨테이너 시작 시 실행될 명령
ENTRYPOINT ["java", "-jar", "app.jar"]