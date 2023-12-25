#!/bin/bash

proc="Master-0.0.1-SNAPSHOT.jar"
# jar이 실행 중인지 확인
pid=$(pgrep -f $proc)

if [ -z "$pid" ]; then
    # JAR 파일이 실행 중이지 않다면, nohup을 사용하여 백그라운드에서 실행
    nohup java -jar -Duser.timezone=Asia/Seoul /home/jeongrae/MasterServer/build/libs/$proc &

    pid=$!
    echo -e "Running \e[34m$pid\e[0m"
else
    # JAR 파일이 이미 실행 중이면, 실행 중인 PID 출력
    echo -e "Already running \e[34m$pid\e[0m"
fi
