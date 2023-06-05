FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /shop_recruitment
COPY . /shop_recruitment
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jdk-alpine
COPY --from=builder /shop_recruitment/build/libs/shop_recruitment-1.0.0.jar shop_recruitment.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "shop_recruitment.jar"]
