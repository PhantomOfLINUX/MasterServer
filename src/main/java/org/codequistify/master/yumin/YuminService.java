package org.codequistify.master.yumin;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.*;

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

        System.out.println(generateCode("yumin"));
    }

    private static String generateCode(String username) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        sb.append("yumin-");

        int k;
        char ch;
        for (int i = 0; i < 6; i++) {
            k = random.nextInt(2);
            if (k == 1) {
                ch = (char)(random.nextInt(26) + 'a');
                sb.append(ch);
            }
            else {
                ch = (char)(random.nextInt(10) + '0');
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public List<Task> getTaskList(String code){
        String pk = "";

        if (code.equals("abc")) {
            pk = "yumin-abc-";
            List<Task> taskList = new ArrayList<>();
            taskList.add(new Task(pk + "0","리액트 개념 공부하기", start, end, true));
            taskList.add(new Task(pk + "1","TODO LIST 만들기", start, end, true));
            taskList.add(new Task(pk + "2","GET 요청 실습하기", start, end, false));
            taskList.add(new Task(pk + "3","POST 요청 실습하기", start, end, false));
            taskList.add(new Task(pk + "4","http status code로 response 구분해보기", start, end, false));
            return taskList;
        }
        else if (code.equals("123")) {
            pk = "yumin-123-";
            List<Task> taskList = new ArrayList<>();
            taskList.add(new Task(pk + "0", "리액트란 무엇인가", start, end, false));
            taskList.add(new Task(pk + "1", "CORS란 무엇인가", start, end, false));
            taskList.add(new Task(pk + "2","훅이란? 활용법 확인", start, end, false));
            taskList.add(new Task(pk + "3","Fetch와 Axios 비교해보기", start, end, false));
            return taskList;
        }
        else {
            List<Task> taskList = new ArrayList<>();
            return taskList;
        }
    }
}
