FROM maven:3.9-eclipse-temurin-25-alpine AS builder
WORKDIR /app

COPY match-movie/pom.xml .
RUN mvn dependency:go-offline -B

COPY match-movie/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]