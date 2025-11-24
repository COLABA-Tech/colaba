FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY api-gateway/pom.xml .
COPY shared/pom.xml ../shared/
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -pl api-gateway -B

COPY api-gateway/src src
COPY shared/src ../shared/src
RUN ./mvnw clean package -pl api-gateway -DskipTests -B

FROM eclipse-temurin:25-jre
RUN useradd -m spring
USER spring
WORKDIR /app
COPY --from=builder /workspace/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]