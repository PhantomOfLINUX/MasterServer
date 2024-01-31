package org.codequistify.master.yumin.dto;

import java.util.List;

public record ListSaveRequest(
        String author,
        List<TaskDTO> taskList
) {
}
