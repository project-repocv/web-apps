# Multi-stage build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/transaction-service-*.jar transaction-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "transaction-service.jar"]
