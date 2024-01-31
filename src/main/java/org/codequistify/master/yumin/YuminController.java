package org.codequistify.master.yumin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codequistify.master.yumin.domain.Task;
import org.codequistify.master.yumin.domain.TodoList;
import org.codequistify.master.yumin.dto.ListSaveRequest;
import org.codequistify.master.yumin.dto.ListSaveResponse;
import org.codequistify.master.yumin.dto.TaskResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/todo-list")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TODO-LIST")
public class YuminController {
    private final YuminService yuminService;


    @GetMapping("task/{code}")
    @Operation(summary = "작업 목록 불러오기", description = "abc, 123 두 코드만 리스트있음. 나머지 요청은 404 NOT_FOUND")
    public ResponseEntity<?> getTaskList(@NonNull @PathVariable String code){
        List<Task> taskList = yuminService.getTaskList(code);
        String author = yuminService.findAuthor(code);

        TaskResponse taskResponse = new TaskResponse(author, taskList);

        if (taskList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        log.info("[TaskList] {}", code);
        return new ResponseEntity<>(taskResponse, HttpStatus.OK);
    }

    @PostMapping("task")
    @Operation(summary = "작업 목록 저장", description = ".")
    public ResponseEntity<?> saveTaskList(@RequestBody ListSaveRequest listSaveRequest) {
        String code = yuminService.saveTodoList(listSaveRequest);
        ListSaveResponse response = new ListSaveResponse(code);

        log.info("[saveTaskList] {}", code);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("task")
    public ResponseEntity<List<TodoList>> getTodoList() {
        log.info("[getTodoList]");

        return new ResponseEntity<>(yuminService.findTodoList(), HttpStatus.OK);
    }

}
