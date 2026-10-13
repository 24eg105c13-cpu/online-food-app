# Multi-stage Dockerfile for Online Food Ordering System
# Stage 1: Build the application
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Production runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV PORT=8080
EXPOSE 8080

COPY --from=builder /app/target/food-ordering-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
