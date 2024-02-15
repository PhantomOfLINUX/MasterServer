FROM openjdk:17-alpine as run

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN mkdir logs

ENTRYPOINT ["nohup", "java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar", ">", "/app/logs/log 2>&1 &"]