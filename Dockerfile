# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre AS runtime

ARG CURL_VERSION=8.5.0-2ubuntu10.6

ENV SERVER_PORT=2828
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENV UPLOAD_DIR=/app/uploads

WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring --home /app spring \
    && apt-get update \
    && apt-get install -y --no-install-recommends "curl=${CURL_VERSION}" \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/target/*.jar /app/app.jar

RUN mkdir -p "${UPLOAD_DIR}" && chown -R spring:spring /app

USER spring

EXPOSE 2828

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 CMD curl -fsS http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["sh","-c","exec java ${JAVA_OPTS} -Dserver.port=${SERVER_PORT} -Dspring.servlet.multipart.location=${UPLOAD_DIR} -jar /app/app.jar"]
