# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY . .

# Build (run with --no-cache if pom/deps change)
RUN ./mvnw package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 app && adduser -u 1000 -G app -D app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
