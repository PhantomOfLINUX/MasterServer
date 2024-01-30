package org.codequistify.master.yumin;

import java.util.Date;

public record Task(
        String id,
        String description,
        Date startDate,
        Date endDate,
        Boolean isDone) {
}
