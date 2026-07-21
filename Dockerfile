# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle/ gradle/
RUN ./gradlew dependencies --no-daemon

COPY src/ src/
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 3002

# Render sets $PORT at runtime; map it to $APP_PORT so Spring picks it up
# Pass all env vars through to the Java process
ENTRYPOINT ["sh", "-c", "APP_PORT=${PORT:-3002} java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} -jar app.jar"]
