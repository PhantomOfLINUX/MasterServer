package org.codequistify.master.domain.stage.dto;

import java.time.LocalDate;

public record HeatMapDataPoint(
        LocalDate date,
        Long count
) {
}