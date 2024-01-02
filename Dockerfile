FROM openjdk:17-alpine as build
LABEL authors="jeongrae"

WORKDIR /app

COPY ./ ./

RUN ./gradlew build -x test

FROM openjdk:17-alpine as run

WORKDIR /app

COPY --from=build /app/build/libs/Master-0.0.1-SNAPSHOT.jar /app

ENTRYPOINT ["java","-jar","Master-0.0.1-SNAPSHOT.jar"]
