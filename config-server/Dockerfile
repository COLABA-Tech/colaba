FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY config-server/pom.xml .
COPY shared/pom.xml ../shared/
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -pl config-server -B

COPY config-server/src src
COPY shared/src ../shared/src
RUN ./mvnw clean package -pl config-server -DskipTests -B

FROM eclipse-temurin:25-jre
RUN useradd -m spring
USER spring
WORKDIR /app
COPY --from=builder /workspace/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]