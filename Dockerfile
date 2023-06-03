FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /friendly_fishstick
COPY . /friendly_fishstick
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jdk-alpine
COPY --from=builder /friendly_fishstick/build/libs/friendly_fishstick-1.0.0.jar friendly_fishstick.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "friendly_fishstick.jar"]
