package io.github.joabsonlg.sigac_api.common.base;

import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Base handler class providing common business logic operations.
 * All module handlers should extend this class for consistency.
 *
 * @param <T> the entity type
 * @param <D> the DTO type
 * @param <ID> the entity ID type
 */
public abstract class BaseHandler<T, D, ID> {
    
    /**
     * Converts an entity to DTO.
     * Must be implemented by concrete handlers.
     */
    protected abstract D toDto(T entity);
    
    /**
     * Converts a DTO to entity.
     * Must be implemented by concrete handlers.
     */
    protected abstract T toEntity(D dto);
    
    /**
     * Converts a Flux of entities to a Flux of DTOs.
     */
    protected Flux<D> toDtoFlux(Flux<T> entityFlux) {
        return entityFlux.map(this::toDto);
    }
    
    /**
     * Converts a Mono of entity to a Mono of DTO.
     */
    protected Mono<D> toDtoMono(Mono<T> entityMono) {
        return entityMono.map(this::toDto);
    }
    
    /**
     * Converts a Flux of DTOs to a Flux of entities.
     */
    protected Flux<T> toEntityFlux(Flux<D> dtoFlux) {
        return dtoFlux.map(this::toEntity);
    }
    
    /**
     * Converts a Mono of DTO to a Mono of entity.
     */
    protected Mono<T> toEntityMono(Mono<D> dtoMono) {
        return dtoMono.map(this::toEntity);
    }
    
    /**
     * Creates a paginated response from a list of DTOs.
     */
    protected Mono<PageResponse<D>> createPageResponse(Flux<D> dtoFlux, int page, int size, Mono<Long> totalElements) {
        return Mono.zip(
                dtoFlux.collectList(),
                totalElements,
                (content, total) -> PageResponse.of(content, page, size, total)
        );
    }
    
    /**
     * Creates a simple page response without pagination metadata.
     */
    protected Mono<PageResponse<D>> createSimplePageResponse(Flux<D> dtoFlux) {
        return dtoFlux.collectList()
                .map(PageResponse::of);
    }
    
    /**
     * Validates that an entity exists or throws exception.
     */
    protected Mono<T> validateExists(Mono<T> entityMono, String entityType, ID id) {
        return entityMono.switchIfEmpty(
                Mono.error(new RuntimeException(String.format("%s with id %s not found", entityType, id)))
        );
    }
}
