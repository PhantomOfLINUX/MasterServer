package org.codequistify.master.yumin;

import lombok.RequiredArgsConstructor;
import org.codequistify.master.yumin.domain.Task;
import org.codequistify.master.yumin.domain.TaskRepository;
import org.codequistify.master.yumin.domain.TodoList;
import org.codequistify.master.yumin.domain.TodoListRepository;
import org.codequistify.master.yumin.dto.ListSaveRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class YuminService {
    private final TaskRepository taskRepository;
    private final TodoListRepository todoListRepository;

    private static String generateCode(String username) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

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

    public String saveTodoList(ListSaveRequest listSaveRequest) {
        // code 발급
        String code = generateCode(listSaveRequest.author());

        // list 목록저장
        TodoList todoList = new TodoList(code, listSaveRequest.author(), listSaveRequest.taskList().size());

        todoListRepository.save(todoList);

        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < listSaveRequest.taskList().size(); i++) {
            String idx = String.valueOf(i);
            Task task = new Task(
                    listSaveRequest.author() + "-" + code + "-" + idx,
                    listSaveRequest.taskList().get(i).description(),
                    listSaveRequest.taskList().get(i).startDate(),
                    listSaveRequest.taskList().get(i).endDate(),
                    listSaveRequest.taskList().get(i).isDone());
            taskList.add(task);
        }

        taskRepository.saveAll(taskList);

        return code;
    }

    public List<Task> getTaskList(String code) {

        return taskRepository.findByIdContainingCode(code);
    }

    public String findAuthor(String code) {
        TodoList todoList = todoListRepository.getReferenceById(code);

        return todoList.getAuthor();
    }

    public List<TodoList> findTodoList() {
        return todoListRepository.findAll();
    }
}
