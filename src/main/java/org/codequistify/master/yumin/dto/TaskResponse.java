package org.codequistify.master.yumin.dto;

import org.codequistify.master.yumin.domain.Task;

import java.util.List;

public record TaskResponse(
        String author,
        List<Task> taskList) {
}
