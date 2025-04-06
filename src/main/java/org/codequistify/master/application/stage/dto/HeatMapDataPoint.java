package org.codequistify.master.application.stage.dto;

import java.util.Date;

public record HeatMapDataPoint(
        Date date,
        Long count
) {
}
