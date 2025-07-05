package io.github.joabsonlg.sigac_api.user.validator;

import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.validator.CommonValidator;
import io.github.joabsonlg.sigac_api.user.dto.CreateUserDTO;
import io.github.joabsonlg.sigac_api.user.dto.CreateEmployeeDTO;
import io.github.joabsonlg.sigac_api.user.dto.UpdateUserDTO;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validator for User-related operations.
 * Extends CommonValidator functionality with user-specific validations.
 */
@Component
public class UserValidator {
    
    private final CommonValidator commonValidator;
    
    public UserValidator(CommonValidator commonValidator) {
        this.commonValidator = commonValidator;
    }
    
    /**
     * Validates CreateUserDTO data
     */
    public Mono<Void> validateCreateUser(CreateUserDTO createUserDTO) {
        return Mono.fromRunnable(() -> {
            // Validate CPF
            try {
                commonValidator.validateCpf(createUserDTO.cpf(), "CPF");
            } catch (ValidationException e) {
                throw new ValidationException("CPF inválido");
            }
            
            // Validate email
            try {
                commonValidator.validateEmail(createUserDTO.email(), "Email");
            } catch (ValidationException e) {
                throw new ValidationException("Email inválido");
            }
            
            // Validate phone if provided
            if (createUserDTO.phone() != null && !createUserDTO.phone().isEmpty()) {
                try {
                    commonValidator.validatePhone(createUserDTO.phone(), "Telefone");
                } catch (ValidationException e) {
                    throw new ValidationException("Telefone inválido");
                }
            }
            
            // Validate password strength
            if (!isStrongPassword(createUserDTO.password())) {
                throw new ValidationException(
                    "Senha deve conter pelo menos 6 caracteres, incluindo letras e números"
                );
            }
        });
    }
    
    /**
     * Validates CreateEmployeeDTO data
     */
    public Mono<Void> validateCreateEmployee(CreateEmployeeDTO createEmployeeDTO) {
        return Mono.fromRunnable(() -> {
            // Validate CPF
            try {
                commonValidator.validateCpf(createEmployeeDTO.cpf(), "CPF");
            } catch (ValidationException e) {
                throw new ValidationException("CPF inválido");
            }
            
            // Validate email
            try {
                commonValidator.validateEmail(createEmployeeDTO.email(), "Email");
            } catch (ValidationException e) {
                throw new ValidationException("Email inválido");
            }
            
            // Validate phone if provided
            if (createEmployeeDTO.phone() != null && !createEmployeeDTO.phone().isEmpty()) {
                try {
                    commonValidator.validatePhone(createEmployeeDTO.phone(), "Telefone");
                } catch (ValidationException e) {
                    throw new ValidationException("Telefone inválido");
                }
            }
            
            // Validate password strength
            if (!isStrongPassword(createEmployeeDTO.password())) {
                throw new ValidationException(
                    "Senha deve conter pelo menos 6 caracteres, incluindo letras e números"
                );
            }
            
            // Validate employee role
            if (!isValidEmployeeRole(createEmployeeDTO.role())) {
                throw new ValidationException("Função de funcionário inválida");
            }
        });
    }
    
    /**
     * Validates UpdateUserDTO data
     */
    public Mono<Void> validateUpdateUser(UpdateUserDTO updateUserDTO) {
        return Mono.fromRunnable(() -> {
            // Validate email if provided
            if (updateUserDTO.email() != null && !updateUserDTO.email().isEmpty()) {
                try {
                    commonValidator.validateEmail(updateUserDTO.email(), "Email");
                } catch (ValidationException e) {
                    throw new ValidationException("Email inválido");
                }
            }
            
            // Validate phone if provided
            if (updateUserDTO.phone() != null && !updateUserDTO.phone().isEmpty()) {
                try {
                    commonValidator.validatePhone(updateUserDTO.phone(), "Telefone");
                } catch (ValidationException e) {
                    throw new ValidationException("Telefone inválido");
                }
            }
        });
    }
    
    /**
     * Validates if the CPF format is correct
     */
    public Mono<Void> validateCpf(String cpf) {
        return Mono.fromRunnable(() -> {
            try {
                commonValidator.validateCpf(cpf, "CPF");
            } catch (ValidationException e) {
                throw new ValidationException("CPF inválido");
            }
        });
    }
    
    /**
     * Validates password strength
     */
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }
    
    /**
     * Validates employee role
     */
    private boolean isValidEmployeeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return false;
        }
        
        // Define valid employee roles
        return role.equals("ADMIN") || role.equals("GERENTE") || role.equals("ATENDENTE");
    }
}
