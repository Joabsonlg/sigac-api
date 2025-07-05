package io.github.joabsonlg.sigac_api.user.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ConflictException;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.user.dto.CreateEmployeeDTO;
import io.github.joabsonlg.sigac_api.user.dto.EmployeeDTO;
import io.github.joabsonlg.sigac_api.user.model.Employee;
import io.github.joabsonlg.sigac_api.user.repository.EmployeeRepository;
import io.github.joabsonlg.sigac_api.user.repository.UserRepository;
import io.github.joabsonlg.sigac_api.user.validator.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handler for business logic related to Employee.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class EmployeeHandler extends BaseHandler<Employee, EmployeeDTO, String> {
    
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    
    public EmployeeHandler(EmployeeRepository employeeRepository, UserRepository userRepository, 
                          UserValidator userValidator, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    protected EmployeeDTO toDto(Employee entity) {
        // This method won't be used directly since we need user info
        return new EmployeeDTO(
            entity.userCpf(),
            null, null, null, null, // Will be filled by the methods that fetch user info
            entity.role()
        );
    }
    
    @Override
    protected Employee toEntity(EmployeeDTO dto) {
        return new Employee(dto.cpf(), dto.role());
    }
    
    /**
     * Converts array of user info to EmployeeDTO
     */
    private EmployeeDTO arrayToEmployeeDto(Object[] userInfo) {
        return new EmployeeDTO(
            (String) userInfo[0], // cpf
            (String) userInfo[1], // email
            (String) userInfo[2], // name
            (String) userInfo[3], // address
            (String) userInfo[4], // phone
            (String) userInfo[5]  // role
        );
    }
    
    /**
     * Gets all employees with complete user information
     */
    public Flux<EmployeeDTO> getAll() {
        return employeeRepository.findAllWithUserInfo()
                .map(this::arrayToEmployeeDto);
    }
    
    /**
     * Gets employee by CPF with complete user information
     */
    public Mono<EmployeeDTO> getById(String cpf) {
        return employeeRepository.findByIdWithUserInfo(cpf)
                .map(this::arrayToEmployeeDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Funcionário", cpf)));
    }
    
    /**
     * Gets paginated employees with complete user information
     */
    public Mono<PageResponse<EmployeeDTO>> getAllPaginated(int page, int size) {
        Flux<EmployeeDTO> employees = employeeRepository.findWithUserInfoPaginated(page, size)
                .map(this::arrayToEmployeeDto);
        
        Mono<Long> totalElements = employeeRepository.countAll();
        
        return createPageResponse(employees, page, size, totalElements);
    }
    
    /**
     * Gets employees by role
     */
    public Flux<EmployeeDTO> getByRole(String role) {
        return employeeRepository.findByRole(role)
                .flatMap(employee -> employeeRepository.findByIdWithUserInfo(employee.userCpf())
                        .map(this::arrayToEmployeeDto));
    }
    
    /**
     * Creates a new employee (creates user first, then employee record)
     */
    public Mono<EmployeeDTO> create(CreateEmployeeDTO createEmployeeDTO) {
        return userValidator.validateCreateEmployee(createEmployeeDTO)
                .then(checkIfUserExists(createEmployeeDTO.cpf()))
                .then(checkIfEmailExists(createEmployeeDTO.email()))
                .then(Mono.fromCallable(() -> {
                    String encodedPassword = passwordEncoder.encode(createEmployeeDTO.password());
                    return new io.github.joabsonlg.sigac_api.user.model.User(
                        createEmployeeDTO.cpf(),
                        createEmployeeDTO.email(),
                        createEmployeeDTO.name(),
                        encodedPassword,
                        createEmployeeDTO.address(),
                        createEmployeeDTO.phone()
                    );
                }))
                .flatMap(userRepository::save)
                .flatMap(user -> {
                    Employee employee = Employee.fromUser(user, createEmployeeDTO.role());
                    return employeeRepository.save(employee)
                            .then(getById(user.cpf()));
                });
    }
    
    /**
     * Converts an existing user to an employee
     */
    public Mono<EmployeeDTO> convertUserToEmployee(String cpf, String role) {
        return userRepository.findById(cpf)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário", cpf)))
                .flatMap(user -> employeeRepository.findById(cpf)
                        .hasElement()
                        .flatMap(isEmployee -> {
                            if (isEmployee) {
                                return Mono.error(new ConflictException("Usuário já é um funcionário"));
                            }
                            Employee employee = Employee.fromUser(user, role);
                            return employeeRepository.save(employee)
                                    .then(getById(cpf));
                        }));
    }
    
    /**
     * Updates employee role
     */
    public Mono<EmployeeDTO> updateRole(String cpf, String newRole) {
        return employeeRepository.findById(cpf)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Funcionário", cpf)))
                .map(employee -> employee.withRole(newRole))
                .flatMap(employeeRepository::update)
                .then(getById(cpf));
    }
    
    /**
     * Deletes an employee (removes employee record but keeps user)
     */
    public Mono<Void> delete(String cpf) {
        return employeeRepository.existsByCpf(cpf)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Funcionário", cpf));
                    }
                    return employeeRepository.deleteByCpf(cpf);
                });
    }
    
    /**
     * Deletes an employee and the associated user
     */
    public Mono<Void> deleteWithUser(String cpf) {
        return employeeRepository.existsByCpf(cpf)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Funcionário", cpf));
                    }
                    return employeeRepository.deleteByCpf(cpf)
                            .then(userRepository.deleteByCpf(cpf));
                });
    }
    
    /**
     * Checks if user is an employee
     */
    public Mono<Boolean> isEmployee(String cpf) {
        return employeeRepository.existsByCpf(cpf);
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
