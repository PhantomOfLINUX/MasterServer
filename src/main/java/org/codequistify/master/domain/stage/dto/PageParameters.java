package org.codequistify.master.domain.stage.dto;

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
