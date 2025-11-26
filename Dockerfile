FROM eclipse-temurin:25-jdk AS builder
WORKDIR /colaba

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY pom.xml .
COPY discovery-server/pom.xml discovery-server/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY shared/pom.xml shared/
COPY user-service/pom.xml user-service/
COPY project-service/pom.xml project-service/
COPY task-service/pom.xml task-service/

RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B

COPY discovery-server/src discovery-server/src
COPY config-server/src config-server/src
COPY api-gateway/src api-gateway/src
COPY shared/src shared/src
COPY user-service/src user-service/src
COPY project-service/src project-service/src
COPY task-service/src task-service/src

RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:25-jre AS discovery-server
WORKDIR /colaba
COPY --from=builder /colaba/discovery-server/target/*.jar colaba.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM eclipse-temurin:25-jre AS config-server
WORKDIR /colaba
COPY --from=builder /colaba/config-server/target/*.jar colaba.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM eclipse-temurin:25-jre AS api-gateway
WORKDIR /colaba
COPY --from=builder /colaba/api-gateway/target/*.jar colaba.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM eclipse-temurin:25-jre AS user-service
WORKDIR /colaba
COPY --from=builder /colaba/user-service/target/*.jar colaba.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM eclipse-temurin:25-jre AS project-service
WORKDIR /colaba
COPY --from=builder /colaba/project-service/target/*.jar colaba.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "colaba.jar"]

FROM eclipse-temurin:25-jre AS task-service
WORKDIR /colaba
COPY --from=builder /colaba/task-service/target/*.jar colaba.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "colaba.jar"]