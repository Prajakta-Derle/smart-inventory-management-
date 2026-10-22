package com.inventory.dto;

import org.springframework.data.domain.Page;

/**
 * Shared helper that converts a Spring {@link Page} into the API's
 * {@link PageResponseDTO} and asserts the pagination metadata invariants
 * (Requirements 13.1, 13.2, 13.6 / Property 6).
 *
 * <p>Existing list endpoints already use {@link PageResponseDTO#from(Page)};
 * this utility centralises that conversion and adds a defensive invariant
 * check so callers get a single, verified entry point.
 */
public final class PaginationUtils {

    private PaginationUtils() {
        // utility class — no instances
    }

    public static <T> PageResponseDTO<T> toPageResponse(Page<T> page) {
        PageResponseDTO<T> dto = PageResponseDTO.from(page);
        assertInvariants(dto);
        return dto;
    }

    /**
     * Verifies the metadata invariants described by Property 6:
     * <ul>
     *   <li>totalPages = ceil(totalElements / pageSize) when totalElements &gt; 0, else 0</li>
     *   <li>content size never exceeds pageSize</li>
     *   <li>first = (currentPage == 0)</li>
     *   <li>last  = (currentPage == totalPages - 1), or true when there are no pages</li>
     * </ul>
     */
    private static <T> void assertInvariants(PageResponseDTO<T> dto) {
        int pageSize      = dto.getPageSize();
        int currentPage   = dto.getCurrentPage();
        long totalElements = dto.getTotalElements();
        int totalPages    = dto.getTotalPages();

        int expectedTotalPages = (totalElements == 0 || pageSize == 0)
                ? 0
                : (int) ((totalElements + pageSize - 1) / pageSize);

        if (totalPages != expectedTotalPages) {
            throw new IllegalStateException(
                    "Pagination invariant violated: totalPages=" + totalPages
                            + " but expected " + expectedTotalPages);
        }
        if (dto.getContent() != null && pageSize > 0 && dto.getContent().size() > pageSize) {
            throw new IllegalStateException(
                    "Pagination invariant violated: content size exceeds pageSize");
        }
        if (dto.isFirst() != (currentPage == 0)) {
            throw new IllegalStateException(
                    "Pagination invariant violated: first flag inconsistent with currentPage");
        }
        boolean expectedLast = totalPages == 0 || currentPage == totalPages - 1;
        if (dto.isLast() != expectedLast) {
            throw new IllegalStateException(
                    "Pagination invariant violated: last flag inconsistent with currentPage/totalPages");
        }
    }
}
