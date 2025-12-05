# ----------------------------------------------------
# 1. 빌드 스테이지 (Build Stage) - JAR 파일 생성
# ----------------------------------------------------
# JDK 17을 사용한 빌드 환경 설정
FROM gradle:8.8-jdk17-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# 1. 필수 Gradle 루트 파일 복사 및 권한 부여
# gradlew 실행, Wrapper 설정, 루트 설정 파일들을 모두 복사합니다.
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle ./gradle

# 2. 모든 모듈의 build.gradle 파일 복사
COPY common/build.gradle common/
COPY config-server/build.gradle config-server/
COPY eureka-server/build.gradle eureka-server/
COPY betting-service/build.gradle betting-service/
COPY user-service/build.gradle user-service/
COPY game-service/build.gradle game-service/
COPY point-service/build.gradle point-service/
COPY api-gateway/build.gradle api-gateway/

RUN chmod +x ./gradlew \
    && ./gradlew :eureka-server:classes -x test -no-daemon

# 4. 소스 코드 복사 (핵심 최적화: 'src' 디렉토리만 복사)
COPY common/src common/src
COPY config-server/src config-server/src
COPY eureka-server/src eureka-server/src
COPY betting-service/src betting-service/src
COPY user-service/src user-service/src
COPY game-service/src game-service/src
COPY point-service/src point-service/src
COPY api-gateway/src api-gateway/src

# 유레카 서버 JAR 파일 빌드 실행
RUN ./gradlew :eureka-server:bootJar -x test --no-daemon

# ----------------------------------------------------
# 2. 최종 실행 스테이지 (Final Run Stage)
# ----------------------------------------------------
# 가벼운 JRE 17 환경 이미지 사용
FROM eclipse-temurin:17-jdk-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 복사
COPY --from=build /app/eureka-server/build/libs/eureka-server-*.jar app.jar

# 유레카 서버의 기본 포트(8761) 노출
EXPOSE 8761

# 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]