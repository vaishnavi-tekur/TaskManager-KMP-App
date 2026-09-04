# Stage 1: Build using an image with Android SDK
FROM mobiledevops/android-sdk-image:34-jdk21 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew :backend:jar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/build/libs/backend.jar app.jar
RUN mkdir -p data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
