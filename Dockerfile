FROM eclipse-temurin:25-jdk AS builder
WORKDIR /colaba
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:25-jre
RUN useradd -m spring
USER spring
WORKDIR /colaba
COPY --from=builder /colaba/target/*.jar /colaba/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/colaba/app.jar"]