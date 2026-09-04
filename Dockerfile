# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy everything first
COPY . .

# Fix permissions for the build script
RUN chmod +x gradlew

# Build only the backend
RUN ./gradlew :backend:assemble --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
RUN mkdir -p data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
