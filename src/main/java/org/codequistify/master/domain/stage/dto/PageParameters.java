package org.codequistify.master.domain.stage.dto;

public record PageParameters(
        int totalPages,
        int pageSize,
        int currentPageIndex,
        int currentPageOfElement,
        int totalElement
) {
}
