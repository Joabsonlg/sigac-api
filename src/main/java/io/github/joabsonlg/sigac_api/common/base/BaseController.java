package io.github.joabsonlg.sigac_api.common.base;

import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Base controller class providing common REST endpoint patterns.
 * All module controllers should extend this class for consistency.
 *
 * @param <D> the DTO type
 * @param <ID> the entity ID type
 */
public abstract class BaseController<D, ID> {
    
    /**
     * Creates a success response with data.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> ok(Mono<T> data) {
        return data.map(result -> ResponseEntity.ok(ApiResponse.success(result)));
    }
    
    /**
     * Creates a success response with data and custom message.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> ok(Mono<T> data, String message) {
        return data.map(result -> ResponseEntity.ok(ApiResponse.success(message, result)));
    }
    
    /**
     * Creates a success response for list data.
     */
    protected Mono<ResponseEntity<ApiResponse<PageResponse<D>>>> okList(Flux<D> data) {
        return data.collectList()
                .map(PageResponse::of)
                .map(pageResponse -> ResponseEntity.ok(ApiResponse.success(pageResponse)));
    }
    
    /**
     * Creates a success response for paginated data.
     */
    protected Mono<ResponseEntity<ApiResponse<PageResponse<D>>>> okPage(Mono<PageResponse<D>> pageResponse) {
        return pageResponse.map(response -> ResponseEntity.ok(ApiResponse.success(response)));
    }
    
    /**
     * Creates a created (201) response.
     */
    protected <T> Mono<ResponseEntity<ApiResponse<T>>> created(Mono<T> data) {
        return data.map(result -> ResponseEntity.status(201).body(ApiResponse.success("Resource created successfully", result)));
    }
    
    /**
     * Creates a success response with only message (no data).
     */
    protected Mono<ResponseEntity<ApiResponse<Void>>> okMessage(String message) {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(message)));
    }
    
    /**
     * Creates a no content (204) response.
     */
    protected Mono<ResponseEntity<Void>> noContent() {
        return Mono.just(ResponseEntity.noContent().build());
    }
    
    /**
     * Validates pagination parameters and returns default values if invalid.
     */
    protected PaginationParams validatePagination(Integer page, Integer size) {
        int validPage = (page != null && page >= 0) ? page : 0;
        int validSize = (size != null && size > 0 && size <= 100) ? size : 20;
        return new PaginationParams(validPage, validSize);
    }
    
    /**
     * Record to hold validated pagination parameters.
     */
    protected record PaginationParams(int page, int size) {}
}
