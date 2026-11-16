# ==============================================================================
# Multi-Stage Dockerfile: EngineerBiz Backend
# Civil Engineering Business Management System
# Java 21 LTS + Spring Boot
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Application
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy Gradle wrapper and configuration files first for Docker layer caching
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew && ./gradlew --version

# Copy source code and build production jar
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Extract layers for optimal Spring Boot layer caching
WORKDIR /build/layers
RUN java -Djarmode=tools -jar /build/build/libs/*.jar extract --layers --destination /build/layers

# ------------------------------------------------------------------------------
# Stage 2: Production Minimal Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

# Create dedicated non-root application user and group
RUN groupadd -r engineerbiz && useradd -r -g engineerbiz -d /app -s /sbin/nologin engineerbiz

WORKDIR /app

# Copy layered spring boot application from builder
COPY --from=builder --chown=engineerbiz:engineerbiz /build/layers/dependencies/ ./
COPY --from=builder --chown=engineerbiz:engineerbiz /build/layers/spring-boot-loader/ ./
COPY --from=builder --chown=engineerbiz:engineerbiz /build/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=engineerbiz:engineerbiz /build/layers/application/ ./

USER engineerbiz

# Container environment variables
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
