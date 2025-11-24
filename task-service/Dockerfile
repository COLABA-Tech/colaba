FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY task-service/pom.xml .
COPY shared/pom.xml ../shared/
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -pl task-service -B

COPY task-service/src src
COPY shared/src ../shared/src
RUN ./mvnw clean package -pl task-service -DskipTests -B

FROM eclipse-temurin:25-jre
RUN useradd -m spring
USER spring
WORKDIR /app
COPY --from=builder /workspace/task-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]