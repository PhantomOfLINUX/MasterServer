package org.codequistify.master.domain.stage.dto;

import java.util.Date;

public record HeatMapDataPoint(
        Date date,
        Long count
) {
}
