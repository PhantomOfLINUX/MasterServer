package org.codequistify.master.yumin;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YuminService {
    private Date start;
    private Date end;

    @PostConstruct
    private void serDate(){
        Calendar calendar = Calendar.getInstance();

        // 1월 1일 설정
        calendar.set(Calendar.YEAR, 2024);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        this.start = calendar.getTime();

        // 3월 1일 설정
        calendar.set(Calendar.MONTH, Calendar.MARCH);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        this.end = calendar.getTime();
    }

    public List<Task> getTaskList(String code){
        if (code.equals("abc")) {
            List<Task> taskList = new ArrayList<>();
            taskList.add(new Task("리액트 개념 공부하기", start, end, true));
            taskList.add(new Task("TODO LIST 만들기", start, end, true));
            taskList.add(new Task("GET 요청 실습하기", start, end, false));
            taskList.add(new Task("POST 요청 실습하기", start, end, false));
            taskList.add(new Task("http status code로 response 구분해보기", start, end, false));
            return taskList;
        }
        else if (code.equals("123")) {
            List<Task> taskList = new ArrayList<>();
            taskList.add(new Task("리액트란 무엇인가", start, end, false));
            taskList.add(new Task("CORS란 무엇인가", start, end, false));
            taskList.add(new Task("훅이란? 활용법 확인", start, end, false));
            taskList.add(new Task("Fetch와 Axios 비교해보기", start, end, false));
            return taskList;
        }
        else {
            List<Task> taskList = new ArrayList<>();
            return taskList;
        }
    }
}
