# Build stage (Debian base for multi-arch: amd64 + arm64/Apple Silicon)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY . .

# Build (run with --no-cache if pom/deps change)
RUN ./mvnw package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user (Debian uses addgroup/adduser with different syntax)
RUN groupadd -g 1001 app && useradd -u 1001 -g app -m app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
