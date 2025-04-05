package org.codequistify.master.core.domain.stage.dto;

public record PageParameters(
        int totalPages,
        int pageSize,
        int currentPageIndex,
        int currentPageOfElement,
        int totalElement
) {
    public static PageParameters of(int totalPages,
                                    int pageSize,
                                    int currentPageIndex,
                                    int currentPageOfElement,
                                    int totalElement) {
        return new PageParameters(
                totalPages,
                pageSize,
                currentPageIndex,
                currentPageOfElement,
                totalElement);
    }
}
