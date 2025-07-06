package io.github.joabsonlg.sigac_api.dailyRate.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.dailyRate.dto.DailyRateInputDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Validator para operações relacionadas à diária (DailyRate).
 */
@Component
public class DailyRateValidator {

    /**
     * Valida os dados para criação ou atualização de uma diária.
     */
    public Mono<Void> validateDailyRate(DailyRateInputDTO dto) {
        return Mono.fromRunnable(() -> {

            if (dto.amount() == null || dto.amount() < 0) {
                throw new ValidationException("O valor da diária deve ser informado e ser maior ou igual a zero");
            }

            if (dto.dateTime() == null) {
                throw new ValidationException("A data e hora da diária são obrigatórias");
            }

            if (dto.dateTime().isAfter(LocalDateTime.now())) {
                throw new ValidationException("A data da diária não pode ser no futuro");
            }

            if (isNullOrEmpty(dto.vehiclePlate())) {
                throw new ValidationException("A placa do veículo é obrigatória");
            }

            validatePlate(dto.vehiclePlate());
        });
    }

    /**
     * Valida se a placa é válida no padrão antigo ou Mercosul.
     *
     * @param plate placa do veículo
     * @return placa normalizada se válida
     */
    public Mono<String> validatePlate(String plate) {
        if (isNullOrEmpty(plate)) {
            return Mono.error(new ValidationException("Placa do veículo é obrigatória"));
        }

        String normalized = plate.replaceAll("-", "").toUpperCase();

        boolean isOldFormat = normalized.matches("^[A-Z]{3}\\d{4}$");
        boolean isMercosul = normalized.matches("^[A-Z]{3}\\d[A-Z]\\d{2}$");

        if (!isOldFormat && !isMercosul) {
            return Mono.error(new ValidationException("Formato de placa inválido. Ex: ABC1234 ou ABC1D23"));
        }

        return Mono.just(normalized);
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}