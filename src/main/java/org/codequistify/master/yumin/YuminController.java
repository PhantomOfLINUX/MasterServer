package org.codequistify.master.yumin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/todo-list")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TODO-LIST")
public class YuminController {
    private final YuminService yuminService;

    @GetMapping("task/{code}")
    @Operation(summary = "list 목록", description = "abc, 123 두 코드만 리스트있음. 나머지 요청은 404 NOT_FOUND")
    public ResponseEntity<?> TaskList(@NonNull @PathVariable String code){
        List<Task> taskList = yuminService.getTaskList(code);
        Response response = new Response("yumin", taskList);

        if (taskList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        log.info("[TaskList] {}", code);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
