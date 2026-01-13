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

RUN ./mvnw clean package -B -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:25-jre AS base
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

FROM base AS discovery-server
WORKDIR /colaba
COPY --from=builder /colaba/discovery-server/target/*.jar colaba.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM base AS config-server
WORKDIR /colaba
COPY --from=builder /colaba/config-server/target/*.jar colaba.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM base AS api-gateway
WORKDIR /colaba
COPY --from=builder /colaba/api-gateway/target/*.jar colaba.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM base AS user-service
WORKDIR /colaba
COPY --from=builder /colaba/user-service/target/*.jar colaba.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM base AS project-service
WORKDIR /colaba
COPY --from=builder /colaba/project-service/target/*.jar colaba.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM base AS task-service
WORKDIR /colaba
COPY --from=builder /colaba/task-service/target/*.jar colaba.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM base AS auth-service
WORKDIR /colaba
COPY --from=builder /colaba/auth-service/target/*.jar colaba.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "colaba.jar"]