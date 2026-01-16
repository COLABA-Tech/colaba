FROM eclipse-temurin:25-jdk AS builder
WORKDIR /colaba

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY shared-common/pom.xml shared-common/
COPY shared-webmvc/pom.xml shared-webmvc/
COPY shared-webflux/pom.xml shared-webflux/
COPY user-service/pom.xml user-service/
COPY project-service/pom.xml project-service/
COPY task-service/pom.xml task-service/
COPY auth-service/pom.xml auth-service/

RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B

COPY discovery-server/src discovery-server/src
COPY config-server/src config-server/src
COPY api-gateway/src api-gateway/src
COPY shared-common/src shared-common/src
COPY shared-webmvc/src shared-webmvc/src
COPY shared-webflux/src shared-webflux/src
COPY user-service/src user-service/src
COPY project-service/src project-service/src
COPY task-service/src task-service/src
COPY auth-service/src auth-service/src

RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -B

FROM eclipse-temurin:25-jre AS base
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

FROM base AS discovery-server
WORKDIR /colaba
COPY --from=builder /colaba/discovery-server/target/discovery-server-*.jar discovery-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "discovery-server.jar"]

FROM base AS config-server
WORKDIR /colaba
COPY --from=builder /colaba/config-server/target/config-server-*.jar config-server.jar
EXPOSE 8888
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "config-server.jar"]

FROM base AS api-gateway
WORKDIR /colaba
COPY --from=builder /colaba/api-gateway/target/api-gateway-*.jar api-gateway.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "api-gateway.jar"]

FROM base AS user-service
WORKDIR /colaba
COPY --from=builder /colaba/user-service/target/user-service-*.jar user-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "user-service.jar"]

FROM base AS project-service
WORKDIR /colaba
COPY --from=builder /colaba/project-service/target/project-service-*.jar project-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "project-service.jar"]

FROM base AS task-service
WORKDIR /colaba
COPY --from=builder /colaba/task-service/target/task-service-*.jar task-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "task-service.jar"]

FROM base AS auth-service
WORKDIR /colaba
COPY --from=builder /colaba/auth-service/target/auth-service-*.jar auth-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "auth-service.jar"]