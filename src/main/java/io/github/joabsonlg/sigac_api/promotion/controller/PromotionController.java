package io.github.joabsonlg.sigac_api.promotion.controller;

import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.promotion.dto.CreatePromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.dto.PromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.dto.UpdatePromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;
import io.github.joabsonlg.sigac_api.promotion.handler.PromotionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller that exposes REST endpoints for Promotion management.
 * Extends BaseController for standardized HTTP responses.
 */
@RestController
@RequestMapping("/api/promotions")
@Tag(name = "Promotion", description = "Operations related to promotion management")
public class PromotionController extends BaseController<PromotionDTO, Integer> {
    
    private final PromotionHandler promotionHandler;
    
    public PromotionController(PromotionHandler promotionHandler) {
        this.promotionHandler = promotionHandler;
    }
    
    /**
     * Gets all promotions with optional filtering and pagination.
     */
    @GetMapping
    @Operation(summary = "Get all promotions", description = "Retrieves a paginated list of promotions with optional filtering by status")
    public Mono<ResponseEntity<ApiResponse<PageResponse<PromotionDTO>>>> getAllPromotions(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") 
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by promotion status") 
            @RequestParam(required = false) PromotionStatus status) {
        
        PaginationParams params = validatePagination(page, size);
        return okPage(promotionHandler.getAllPaginated(params.page(), params.size(), status));
    }
    
    /**
     * Gets a specific promotion by code.
     */
    @GetMapping("/{code}")
    @Operation(summary = "Get promotion by code", description = "Retrieves a specific promotion by its code")
    public Mono<ResponseEntity<ApiResponse<PromotionDTO>>> getPromotionByCode(
            @Parameter(description = "Promotion code") 
            @PathVariable Integer code) {
        return ok(promotionHandler.getById(code));
    }
    
    /**
     * Creates a new promotion.
     */
    @PostMapping
    @Operation(summary = "Create new promotion", description = "Creates a new promotion")
    public Mono<ResponseEntity<ApiResponse<PromotionDTO>>> createPromotion(
            @RequestBody CreatePromotionDTO createPromotionDTO) {
        return created(promotionHandler.create(createPromotionDTO));
    }
    
    /**
     * Updates an existing promotion.
     */
    @PutMapping("/{code}")
    @Operation(summary = "Update promotion", description = "Updates an existing promotion")
    public Mono<ResponseEntity<ApiResponse<PromotionDTO>>> updatePromotion(
            @Parameter(description = "Promotion code") 
            @PathVariable Integer code,
            @RequestBody UpdatePromotionDTO updatePromotionDTO) {
        return ok(promotionHandler.update(code, updatePromotionDTO));
    }
    
    /**
     * Deletes a promotion.
     */
    @DeleteMapping("/{code}")
    @Operation(summary = "Delete promotion", description = "Deletes a promotion (only if not active)")
    public Mono<ResponseEntity<Void>> deletePromotion(
            @Parameter(description = "Promotion code") 
            @PathVariable Integer code) {
        return promotionHandler.delete(code)
                .then(noContent());
    }
    
    /**
     * Activates a promotion.
     */
    @PatchMapping("/{code}/activate")
    @Operation(summary = "Activate promotion", description = "Activates a promotion")
    public Mono<ResponseEntity<ApiResponse<PromotionDTO>>> activatePromotion(
            @Parameter(description = "Promotion code") 
            @PathVariable Integer code) {
        return ok(promotionHandler.activate(code));
    }
    
    /**
     * Deactivates a promotion.
     */
    @PatchMapping("/{code}/deactivate")
    @Operation(summary = "Deactivate promotion", description = "Deactivates a promotion")
    public Mono<ResponseEntity<ApiResponse<PromotionDTO>>> deactivatePromotion(
            @Parameter(description = "Promotion code") 
            @PathVariable Integer code) {
        return ok(promotionHandler.deactivate(code));
    }
    
    /**
     * Gets active promotions.
     */
    @GetMapping("/active")
    @Operation(summary = "Get active promotions", description = "Retrieves all currently active promotions")
    public Mono<ResponseEntity<ApiResponse<PageResponse<PromotionDTO>>>> getActivePromotions() {
        return okList(promotionHandler.getActivePromotions());
    }
    
    /**
     * Gets promotions by status.
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get promotions by status", description = "Retrieves all promotions with a specific status")
    public Mono<ResponseEntity<ApiResponse<PageResponse<PromotionDTO>>>> getPromotionsByStatus(
            @Parameter(description = "Promotion status") 
            @PathVariable PromotionStatus status) {
        return okList(promotionHandler.getByStatus(status));
    }
}
