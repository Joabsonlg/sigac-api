package io.github.joabsonlg.sigac_api.user.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ConflictException;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.exception.ValidationException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.user.dto.*;
import io.github.joabsonlg.sigac_api.user.model.User;
import io.github.joabsonlg.sigac_api.user.repository.UserRepository;
import io.github.joabsonlg.sigac_api.user.validator.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handler for business logic related to User.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class UserHandler extends BaseHandler<User, UserDTO, String> {
    
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    
    public UserHandler(UserRepository userRepository, UserValidator userValidator, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    protected UserDTO toDto(User entity) {
        return new UserDTO(
            entity.cpf(),
            entity.email(),
            entity.name(),
            entity.address(),
            entity.phone()
        );
    }
    
    @Override
    protected User toEntity(UserDTO dto) {
        // This method is used for updates, so password is null
        return new User(
            dto.cpf(),
            dto.email(),
            dto.name(),
            null, // Password handled separately
            dto.address(),
            dto.phone()
        );
    }
    
    /**
     * Gets all users
     */
    public Flux<UserDTO> getAll() {
        return toDtoFlux(userRepository.findAll());
    }
    
    /**
     * Gets user by CPF
     */
    public Mono<UserDTO> getById(String cpf) {
        return userRepository.findById(cpf)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário", cpf)));
    }
    
    /**
     * Gets user by email
     */
    public Mono<UserDTO> getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário com email", email)));
    }
    
    /**
     * Gets paginated users
     */
    public Mono<PageResponse<UserDTO>> getAllPaginated(int page, int size) {
        Flux<UserDTO> users = toDtoFlux(userRepository.findWithPagination(page, size));
        Mono<Long> totalElements = userRepository.countAll();
        
        return createPageResponse(users, page, size, totalElements);
    }
    
    /**
     * Creates a new user
     */
    public Mono<UserDTO> create(CreateUserDTO createUserDTO) {
        return userValidator.validateCreateUser(createUserDTO)
                .then(checkIfUserExists(createUserDTO.cpf()))
                .then(checkIfEmailExists(createUserDTO.email()))
                .then(Mono.fromCallable(() -> {
                    String encodedPassword = passwordEncoder.encode(createUserDTO.password());
                    return new User(
                        createUserDTO.cpf(),
                        createUserDTO.email(),
                        createUserDTO.name(),
                        encodedPassword,
                        createUserDTO.address(),
                        createUserDTO.phone()
                    );
                }))
                .flatMap(userRepository::save)
                .map(this::toDto);
    }
    
    /**
     * Updates an existing user
     */
    public Mono<UserDTO> update(String cpf, UpdateUserDTO updateUserDTO) {
        return userValidator.validateUpdateUser(updateUserDTO)
                .then(userRepository.findById(cpf))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário", cpf)))
                .flatMap(existingUser -> {
                    if (updateUserDTO.email() != null && !updateUserDTO.email().equals(existingUser.email())) {
                        return userRepository.existsByEmailAndCpfNot(updateUserDTO.email(), cpf)
                                .flatMap(emailExists -> {
                                    if (emailExists) {
                                        return Mono.error(new ConflictException("Email já está em uso"));
                                    }
                                    return Mono.just(existingUser);
                                });
                    }
                    return Mono.just(existingUser);
                })
                .map(existingUser -> existingUser.withUpdatedInfo(
                    updateUserDTO.email() != null ? updateUserDTO.email() : existingUser.email(),
                    updateUserDTO.name() != null ? updateUserDTO.name() : existingUser.name(),
                    updateUserDTO.address() != null ? updateUserDTO.address() : existingUser.address(),
                    updateUserDTO.phone() != null ? updateUserDTO.phone() : existingUser.phone()
                ))
                .flatMap(userRepository::update)
                .map(this::toDto);
    }
    
    /**
     * Changes user password
     */
    public Mono<Void> changePassword(String cpf, ChangePasswordDTO changePasswordDTO) {
        return userRepository.findById(cpf)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário", cpf)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(changePasswordDTO.currentPassword(), user.password())) {
                        return Mono.error(new ValidationException("Senha atual incorreta"));
                    }
                    String encodedNewPassword = passwordEncoder.encode(changePasswordDTO.newPassword());
                    return userRepository.updatePassword(cpf, encodedNewPassword);
                });
    }
    
    /**
     * Deletes a user
     */
    public Mono<Void> delete(String cpf) {
        return userRepository.existsByCpf(cpf)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Usuário", cpf));
                    }
                    return userRepository.deleteByCpf(cpf);
                });
    }
    
    /**
     * Checks if user exists by CPF
     */
    public Mono<Boolean> existsByCpf(String cpf) {
        return userRepository.existsByCpf(cpf);
    }
    
    /**
     * Checks if email exists
     */
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Helper method to check if user already exists
     */
    private Mono<Void> checkIfUserExists(String cpf) {
        return userRepository.existsByCpf(cpf)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Usuário com CPF " + cpf + " já existe"));
                    }
                    return Mono.empty();
                });
    }
    
    /**
     * Helper method to check if email already exists
     */
    private Mono<Void> checkIfEmailExists(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("Email já está em uso"));
                    }
                    return Mono.empty();
                });
    }
}
