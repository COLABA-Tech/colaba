FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY discovery-server/pom.xml .
COPY shared/pom.xml ../shared/
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -pl discovery-server -B

COPY discovery-server/src src
COPY shared/src ../shared/src
RUN ./mvnw clean package -pl discovery-server -DskipTests -B

FROM eclipse-temurin:25-jre
RUN useradd -m spring
USER spring
WORKDIR /app
COPY --from=builder /workspace/discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]