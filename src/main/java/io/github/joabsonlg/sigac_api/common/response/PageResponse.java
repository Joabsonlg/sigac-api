package io.github.joabsonlg.sigac_api.common.response;

import java.util.List;

/**
 * Paginated response wrapper for list endpoints.
 * Provides pagination metadata along with the data.
 *
 * @param <T> the type of items in the page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    
    /**
     * Creates a PageResponse with pagination metadata.
     */
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        return new PageResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }
    
    /**
     * Creates a PageResponse for simple list responses (no pagination).
     */
    public static <T> PageResponse<T> of(List<T> content) {
        return new PageResponse<>(
                content,
                0,
                content.size(),
                content.size(),
                1,
                false,
                false
        );
    }
}
