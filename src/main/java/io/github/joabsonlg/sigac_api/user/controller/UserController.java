package io.github.joabsonlg.sigac_api.user.controller;

import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.user.dto.*;
import io.github.joabsonlg.sigac_api.user.handler.UserHandler;
import io.github.joabsonlg.sigac_api.user.handler.EmployeeHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller that exposes REST endpoints for User and Employee management.
 * Provides user consultation endpoints and complete employee management.
 * Users are created only through employee or client endpoints.
 * Extends BaseController for standardized HTTP responses and patterns.
 */
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<UserDTO, String> {
    
    private final UserHandler userHandler;
    private final EmployeeHandler employeeHandler;
    
    public UserController(UserHandler userHandler, EmployeeHandler employeeHandler) {
        this.userHandler = userHandler;
        this.employeeHandler = employeeHandler;
    }
    
    /**
     * Gets all users
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page >= 0 && size > 0) {
            return userHandler.getAllPaginated(page, size)
                    .map(pageResponse -> ResponseEntity.ok(ApiResponse.success(pageResponse)));
        } else {
            return userHandler.getAll()
                    .collectList()
                    .map(users -> ResponseEntity.ok(ApiResponse.success(users)));
        }
    }
    
    /**
     * Gets user by CPF
     */
    @GetMapping("/{cpf}")
    public Mono<ResponseEntity<ApiResponse<UserDTO>>> getUserById(@PathVariable String cpf) {
        return ok(userHandler.getById(cpf));
    }
    
    /**
     * Gets user by email
     */
    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<ApiResponse<UserDTO>>> getUserByEmail(@PathVariable String email) {
        return ok(userHandler.getByEmail(email));
    }
    
    /**
     * Changes user password
     */
    @PatchMapping("/{cpf}/password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
            @PathVariable String cpf,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        return userHandler.changePassword(cpf, changePasswordDTO)
                .then(okMessage("Senha alterada com sucesso"));
    }
    
    /**
     * Deletes a user
     */
    @DeleteMapping("/{cpf}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteUser(@PathVariable String cpf) {
        return userHandler.delete(cpf)
                .then(okMessage("Usuário excluído com sucesso"));
    }
    
    /**
     * Checks if user exists by CPF
     */
    @GetMapping("/{cpf}/exists")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> userExists(@PathVariable String cpf) {
        return ok(userHandler.existsByCpf(cpf));
    }
    
    /**
     * Checks if email exists
     */
    @GetMapping("/email/{email}/exists")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> emailExists(@PathVariable String email) {
        return ok(userHandler.existsByEmail(email));
    }
    
    // ============= EMPLOYEE ENDPOINTS =============
    
    /**
     * Gets all employees
     */
    @GetMapping("/employees")
    public Mono<ResponseEntity<ApiResponse<Object>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page >= 0 && size > 0) {
            return employeeHandler.getAllPaginated(page, size)
                    .map(pageResponse -> ResponseEntity.ok(ApiResponse.success(pageResponse)));
        } else {
            return employeeHandler.getAll()
                    .collectList()
                    .map(employees -> ResponseEntity.ok(ApiResponse.success(employees)));
        }
    }
    
    /**
     * Gets employee by CPF
     */
    @GetMapping("/employees/{cpf}")
    public Mono<ResponseEntity<ApiResponse<EmployeeDTO>>> getEmployeeById(@PathVariable String cpf) {
        return ok(employeeHandler.getById(cpf));
    }
    
    /**
     * Gets employees by role
     */
    @GetMapping("/employees/role/{role}")
    public Mono<ResponseEntity<ApiResponse<Object>>> getEmployeesByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page >= 0 && size > 0) {
            return employeeHandler.getByRole(role)
                    .skip((long) page * size)
                    .take(size)
                    .collectList()
                    .zipWith(employeeHandler.getByRole(role).count())
                    .map(tuple -> {
                        PageResponse<EmployeeDTO> pageResponse = PageResponse.of(
                            tuple.getT1(), page, size, tuple.getT2()
                        );
                        return ResponseEntity.ok(ApiResponse.success(pageResponse));
                    });
        } else {
            return employeeHandler.getByRole(role)
                    .collectList()
                    .map(employees -> ResponseEntity.ok(ApiResponse.success(employees)));
        }
    }
    
    /**
     * Creates a new employee
     */
    @PostMapping("/employees")
    public Mono<ResponseEntity<ApiResponse<EmployeeDTO>>> createEmployee(@Valid @RequestBody CreateEmployeeDTO createEmployeeDTO) {
        return created(employeeHandler.create(createEmployeeDTO));
    }
    
    /**
     * Converts an existing user to employee
     */
    @PostMapping("/employees/{cpf}/convert")
    public Mono<ResponseEntity<ApiResponse<EmployeeDTO>>> convertUserToEmployee(
            @PathVariable String cpf,
            @RequestParam String role) {
        return created(employeeHandler.convertUserToEmployee(cpf, role));
    }
    
    /**
     * Updates employee role
     */
    @PatchMapping("/employees/{cpf}/role")
    public Mono<ResponseEntity<ApiResponse<EmployeeDTO>>> updateEmployeeRole(
            @PathVariable String cpf,
            @RequestParam String role) {
        return ok(employeeHandler.updateRole(cpf, role));
    }
    
    /**
     * Deletes an employee (keeps the user)
     */
    @DeleteMapping("/employees/{cpf}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteEmployee(@PathVariable String cpf) {
        return employeeHandler.delete(cpf)
                .then(okMessage("Funcionário removido com sucesso"));
    }
    
    /**
     * Deletes an employee and the associated user
     */
    @DeleteMapping("/employees/{cpf}/with-user")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteEmployeeWithUser(@PathVariable String cpf) {
        return employeeHandler.deleteWithUser(cpf)
                .then(okMessage("Funcionário e usuário excluídos com sucesso"));
    }
    
    /**
     * Checks if user is an employee by CPF
     */
    @GetMapping("/employees/{cpf}/exists")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> employeeExists(@PathVariable String cpf) {
        return ok(employeeHandler.isEmployee(cpf));
    }
}
