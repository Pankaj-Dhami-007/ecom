FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar api-gateway.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
