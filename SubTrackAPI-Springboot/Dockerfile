# ==========================================
# Stage 1: Build the application
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml file first to cache dependencies
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Package the application into an executable jar (skipping tests for container speed)
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Runtime environment
# ==========================================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar artifact from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]