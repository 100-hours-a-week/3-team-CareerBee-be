FROM gradle:8.5.0-jdk21 AS builder

WORKDIR /backend
COPY . .

RUN chmod +x ./gradlew

ARG SENTRY_AUTH_TOKEN
ENV SENTRY_AUTH_TOKEN=$SENTRY_AUTH_TOKEN

RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /backend

COPY --from=builder /backend/build/libs/careerbee-api.jar careerbee-api.jar

EXPOSE 8080

CMD ["java", "-jar", "careerbee-api.jar"]