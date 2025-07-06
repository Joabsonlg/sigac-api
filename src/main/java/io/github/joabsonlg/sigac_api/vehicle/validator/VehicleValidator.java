package io.github.joabsonlg.sigac_api.vehicle.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import io.github.joabsonlg.sigac_api.vehicle.dto.CreateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.dto.UpdateVehicleDTO;
import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validator para operações relacionadas a veículos.
 * Estende funcionalidades do CommonValidator com validações específicas para veículos.
 */
@Component
public class VehicleValidator {

    private final CommonValidator commonValidator;

    public VehicleValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }

    /**
     * Valida os dados ao criar um veículo.
     */
    public Mono<Void> validateCreateVehicle(CreateVehicleDTO dto) {
        return Mono.fromRunnable(() -> {
            validatePlate(dto.plate());

            if (dto.year() != null && (dto.year() < 1900 || dto.year() > 2100)) {
                throw new ValidationException("Ano do veículo deve estar entre 1900 e 2100");
            }

            if (isNullOrEmpty(dto.model())) {
                throw new ValidationException("Modelo do veículo é obrigatório");
            }

            if (isNullOrEmpty(dto.brand())) {
                throw new ValidationException("Marca do veículo é obrigatória");
            }

            if (dto.status() == null) {
                throw new ValidationException("Status do veículo é obrigatório");
            }

            if (dto.imageUrl() != null && !dto.imageUrl().isBlank()) {
                try {
                    commonValidator.validateUrl(dto.imageUrl(), "URL da imagem");
                } catch (ValidationException e) {
                    throw new ValidationException("URL da imagem inválida");
                }
            }
        });
    }

    /**
     * Valida os dados ao atualizar um veículo.
     */
    public Mono<Void> validateUpdateVehicle(UpdateVehicleDTO dto) {
        return Mono.fromRunnable(() -> {
            if (dto.year() != null && (dto.year() < 1900 || dto.year() > 2100)) {
                throw new ValidationException("Ano do veículo deve estar entre 1900 e 2100");
            }

            if (dto.imageUrl() != null && !dto.imageUrl().isBlank()) {
                try {
                    commonValidator.validateUrl(dto.imageUrl(), "URL da imagem");
                } catch (ValidationException e) {
                    throw new ValidationException("URL da imagem inválida");
                }
            }
        });
    }

    /**
     * Valida a placa do veículo.
     */
    private void validatePlate(String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            throw new ValidationException("Placa do veículo é obrigatória");
        }

        String normalized = plate.replaceAll("-", "").toUpperCase();

        boolean isOldFormat = normalized.matches("^[A-Z]{3}\\d{4}$");
        boolean isMercosul = normalized.matches("^[A-Z]{3}\\d[A-Z]\\d{2}$");

        if (!isOldFormat && !isMercosul) {
            throw new ValidationException("Formato de placa inválido. Ex: ABC1234 ou ABC1D23");
        }
    }

    /**
     * Verifica se um campo está nulo ou vazio.
     */
    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}