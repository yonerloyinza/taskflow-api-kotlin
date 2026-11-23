# Multi-stage Dockerfile for taskflow-api-kotlin
# VPS AWS EC2 + Traefik: gradle:8.8-jdk21 build -> eclipse-temurin:21-jre-alpine runtime

FROM gradle:8.8-jdk21 AS builder
WORKDIR /app

COPY settings.gradle.kts build.gradle.kts gradle.properties ./
RUN gradle dependencies --no-daemon

COPY src ./src
COPY docs ./docs

RUN gradle shadowJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN apk add --no-cache ca-certificates tzdata wget

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /app/build/libs/*-all.jar app.jar

USER appuser

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=10s --start-period=20s --retries=3 \
    CMD wget -qO- http://localhost:3000/health | grep -q taskflow-api-kotlin || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]