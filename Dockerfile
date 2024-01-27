FROM openjdk:17-alpine as run

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["nohup", "java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar", ">", "/app/logs/log 2>&1 &"]