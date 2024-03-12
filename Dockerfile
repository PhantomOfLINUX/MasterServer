FROM openjdk:17-alpine as run

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN mkdir logs

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar", "&"]