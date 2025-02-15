FROM hseeberger/scala-sbt:eclipse-temurin-11.0.14.1_1.6.2_2.12.15 AS builder

WORKDIR /app
COPY . .
RUN sbt clean assembly

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/scala-2.13/scala-app.jar ./app.jar

CMD ["java", "-jar", "app.jar"]
