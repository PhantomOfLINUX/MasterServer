package org.codequistify.master.core.domain.stage.dto;

import java.util.Date;

public record HeatMapDataPoint(
        Date date,
        Long count
) {
}
