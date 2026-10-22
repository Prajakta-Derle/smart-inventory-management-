# ---- Build stage: compile the Spring Boot jar with Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first (faster rebuilds)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Run stage: slim JRE that runs the packaged jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# The app reads the PORT env var (Render injects it); defaults to 8080.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
