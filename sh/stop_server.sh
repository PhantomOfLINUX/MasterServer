#!/bin/bash

proc="Master-0.0.1-SNAPSHOT.jar"

pid=$(pgrep -f $proc)

if [ -n "$pid" ]; then
    # JAR 파일이 실행 중이라면, 프로세스를 종료
    kill -15 $pid

    # 프로세스가 안전하게 종료되었는지 확인
    if wait $pid 2>/dev/null; then
        echo -e "Stop Server \e[34m$pid\e[0m"
    else
        echo "Error stopping $proc"
    fi
else
    echo "Already Stop"
fi
