package org.codequistify.master.yumin.dto;

import org.codequistify.master.yumin.domain.Task;

import java.util.Date;
import java.util.List;

public record TaskDTO(
        String id,
        String description,
        Date startDate,
        Date endDate,
        Boolean isDone) {
}
