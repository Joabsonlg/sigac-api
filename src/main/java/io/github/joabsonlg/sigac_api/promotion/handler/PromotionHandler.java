package io.github.joabsonlg.sigac_api.promotion.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.promotion.dto.CreatePromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.dto.PromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.dto.UpdatePromotionDTO;
import io.github.joabsonlg.sigac_api.promotion.enumeration.PromotionStatus;
import io.github.joabsonlg.sigac_api.promotion.model.Promotion;
import io.github.joabsonlg.sigac_api.promotion.repository.PromotionRepository;
import io.github.joabsonlg.sigac_api.promotion.validator.PromotionValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Handler for business logic related to Promotion.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class PromotionHandler extends BaseHandler<Promotion, PromotionDTO, Integer> {

    private final PromotionRepository promotionRepository;
    private final PromotionValidator promotionValidator;

    public PromotionHandler(PromotionRepository promotionRepository, PromotionValidator promotionValidator) {
        this.promotionRepository = promotionRepository;
        this.promotionValidator = promotionValidator;
    }

    @Override
    protected PromotionDTO toDto(Promotion entity) {
        return new PromotionDTO(
            entity.code(),
            entity.discountPercentage(),
            entity.status(),
            entity.startDate(),
            entity.endDate(),
            null, // isCurrentlyValid - calculated when needed
            null  // reservationCount - calculated when needed
        );
    }

    @Override
    protected Promotion toEntity(PromotionDTO dto) {
        return new Promotion(
            dto.code(),
            dto.discountPercentage(),
            dto.status(),
            dto.startDate(),
            dto.endDate()
        );
    }

    /**
     * Gets all promotions
     */
    public Flux<PromotionDTO> getAll() {
        return toDtoFlux(promotionRepository.findAll());
    }

    /**
     * Gets promotion by code
     */
    public Mono<PromotionDTO> getById(Integer code) {
        return promotionRepository.findById(code)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Promotion", code.toString())));
    }

    /**
     * Gets paginated promotions with optional status filter
     */
    public Mono<PageResponse<PromotionDTO>> getAllPaginated(int page, int size, PromotionStatus status) {
        Flux<PromotionDTO> promotions;
        Mono<Long> totalElements;
        
        if (status != null) {
            promotions = toDtoFlux(promotionRepository.findByStatus(status, page, size));
            totalElements = promotionRepository.countByStatus(status);
        } else {
            promotions = toDtoFlux(promotionRepository.findWithPagination(page, size));
            totalElements = promotionRepository.countAll();
        }
        
        return createPageResponse(promotions, page, size, totalElements);
    }

    /**
     * Gets promotions by status
     */
    public Flux<PromotionDTO> getByStatus(PromotionStatus status) {
        return toDtoFlux(promotionRepository.findByStatus(status, 0, Integer.MAX_VALUE));
    }

    /**
     * Gets active promotions
     */
    public Flux<PromotionDTO> getActivePromotions() {
        return toDtoFlux(promotionRepository.findCurrentlyValid());
    }

    /**
     * Creates a new promotion
     */
    public Mono<PromotionDTO> create(CreatePromotionDTO createPromotionDTO) {
        return promotionValidator.validateCreatePromotion(createPromotionDTO)
                .then(Mono.fromCallable(() -> {
                    return new Promotion(
                        createPromotionDTO.discountPercentage(),
                        PromotionStatus.SCHEDULED, // Default status
                        createPromotionDTO.startDate(),
                        createPromotionDTO.endDate()
                    );
                }))
                .flatMap(promotionRepository::save)
                .map(this::toDto);
    }

    /**
     * Updates a promotion
     */
    public Mono<PromotionDTO> update(Integer code, UpdatePromotionDTO updatePromotionDTO) {
        return promotionRepository.findById(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Promotion", code.toString())))
                .flatMap(existingPromotion -> 
                    promotionValidator.validateUpdatePromotion(updatePromotionDTO)
                            .then(Mono.fromCallable(() -> updateEntity(existingPromotion, updatePromotionDTO)))
                )
                .flatMap(promotionRepository::save)
                .map(this::toDto);
    }

    /**
     * Deletes a promotion
     */
    public Mono<Void> delete(Integer code) {
        return promotionRepository.findById(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Promotion", code.toString())))
                .flatMap(promotion -> {
                    if (promotion.status() == PromotionStatus.ACTIVE) {
                        return Mono.error(new ValidationException("Cannot delete an active promotion"));
                    }
                    return promotionRepository.deletePromotionByCode(code);
                });
    }

    /**
     * Activates a promotion
     */
    public Mono<PromotionDTO> activate(Integer code) {
        return promotionRepository.findById(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Promotion", code.toString())))
                .flatMap(promotion -> {
                    if (promotion.status() == PromotionStatus.ACTIVE) {
                        return Mono.error(new ValidationException("Promotion is already active"));
                    }
                    
                    LocalDateTime now = LocalDateTime.now();
                    if (promotion.startDate().isAfter(now)) {
                        return Mono.error(new ValidationException("Promotion cannot be activated before start date"));
                    }
                    if (promotion.endDate().isBefore(now)) {
                        return Mono.error(new ValidationException("Promotion cannot be activated after end date"));
                    }
                    
                    Promotion updatedPromotion = promotion.withStatus(PromotionStatus.ACTIVE);
                    return promotionRepository.save(updatedPromotion);
                })
                .map(this::toDto);
    }

    /**
     * Deactivates a promotion
     */
    public Mono<PromotionDTO> deactivate(Integer code) {
        return promotionRepository.findById(code)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Promotion", code.toString())))
                .flatMap(promotion -> {
                    if (promotion.status() == PromotionStatus.INACTIVE) {
                        return Mono.error(new ValidationException("Promotion is already inactive"));
                    }
                    
                    Promotion updatedPromotion = promotion.withStatus(PromotionStatus.INACTIVE);
                    return promotionRepository.save(updatedPromotion);
                })
                .map(this::toDto);
    }

    /**
     * Updates a promotion entity with data from UpdatePromotionDTO
     */
    private Promotion updateEntity(Promotion promotion, UpdatePromotionDTO dto) {
        return new Promotion(
            promotion.code(),
            dto.discountPercentage() != null ? dto.discountPercentage() : promotion.discountPercentage(),
            dto.status() != null ? dto.status() : promotion.status(),
            dto.startDate() != null ? dto.startDate() : promotion.startDate(),
            dto.endDate() != null ? dto.endDate() : promotion.endDate()
        );
    }
}
