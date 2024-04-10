FROM openjdk:17-alpine as run

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN mkdir logs

# swagger 호스트
ENV HOST_API_SERVER=${HOST_API_SERVER}
# swagger app 버전
ENV APP_VERSION=${APP_VERSION}

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dhost.api.server=${HOST_API_SERVER}", "-Dapp.version=${APP_VERSION}", "-jar", "app.jar"]
