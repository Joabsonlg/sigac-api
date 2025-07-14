package io.github.joabsonlg.sigac_api.user.controller;

import io.github.joabsonlg.sigac_api.common.base.BaseController;
import io.github.joabsonlg.sigac_api.common.response.ApiResponse;
import io.github.joabsonlg.sigac_api.user.dto.*;
import io.github.joabsonlg.sigac_api.user.handler.ClientHandler;
import io.github.joabsonlg.sigac_api.user.handler.UserHandler;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller that exposes REST endpoints for Client management.
 * Extends BaseController for standardized HTTP responses and patterns.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController extends BaseController<ClientDTO, String> {
    
    private final ClientHandler clientHandler;
    private final UserHandler userHandler;
    
    public ClientController(ClientHandler clientHandler, UserHandler userHandler) {
        this.clientHandler = clientHandler;
        this.userHandler = userHandler;
    }
    
    /**
     * Gets all clients
     */
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (page >= 0 && size > 0) {
            return clientHandler.getAllPaginated(page, size)
                    .map(pageResponse -> ResponseEntity.ok(ApiResponse.success(pageResponse)));
        } else {
            return clientHandler.getAll()
                    .collectList()
                    .map(clients -> ResponseEntity.ok(ApiResponse.success(clients)));
        }
    }
    
    /**
     * Gets client by CPF
     */
    @GetMapping("/{cpf}")
    public Mono<ResponseEntity<ApiResponse<ClientDTO>>> getClientById(@PathVariable String cpf) {
        return ok(clientHandler.getById(cpf));
    }
    
    /**
     * Gets client by email (delegates to user handler, then checks if client)
     */
    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<ApiResponse<ClientDTO>>> getClientByEmail(@PathVariable String email) {
        return userHandler.getByEmail(email)
                .flatMap(userDTO -> clientHandler.isClient(userDTO.cpf())
                        .flatMap(isClient -> {
                            if (isClient) {
                                return clientHandler.getById(userDTO.cpf());
                            } else {
                                return Mono.error(new io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException("Cliente", email));
                            }
                        }))
                .map(clientDTO -> ResponseEntity.ok(ApiResponse.success(clientDTO)));
    }
    
    /**
     * Creates a new client
     */
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ClientDTO>>> createClient(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return created(clientHandler.create(createUserDTO));
    }

    /**
     * Registers a new client.
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<ClientDTO>>> registerClient(@Valid @RequestBody ClientRegistrationDTO clientRegistrationDTO) {
        return created(clientHandler.registerClient(clientRegistrationDTO));
    }
    
    /**
     * Converts an existing user to client
     */
    @PostMapping("/{cpf}/convert")
    public Mono<ResponseEntity<ApiResponse<ClientDTO>>> convertUserToClient(@PathVariable String cpf) {
        return created(clientHandler.convertUserToClient(cpf));
    }
    
    /**
     * Updates an existing client
     */
    @PutMapping("/{cpf}")
    public Mono<ResponseEntity<ApiResponse<ClientDTO>>> updateClient(
            @PathVariable String cpf,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return ok(userHandler.update(cpf, updateUserDTO).map(ClientDTO::fromUserDTO));
    }
    
    /**
     * Changes client password
     */
    @PatchMapping("/{cpf}/password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
            @PathVariable String cpf,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        return userHandler.changePassword(cpf, changePasswordDTO)
                .then(okMessage("Senha alterada com sucesso"));
    }
    
    /**
     * Deletes a client
     */
    @DeleteMapping("/{cpf}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteClient(@PathVariable String cpf) {
        return clientHandler.delete(cpf)
                .then(okMessage("Cliente excluído com sucesso"));
    }
    
    /**
     * Checks if client exists by CPF
     */
    @GetMapping("/{cpf}/exists")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> clientExists(@PathVariable String cpf) {
        return ok(clientHandler.isClient(cpf));
    }
    
    /**
     * Checks if email exists for clients (delegates to user handler)
     */
    @GetMapping("/email/{email}/exists")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> emailExists(@PathVariable String email) {
        return ok(userHandler.existsByEmail(email));
    }
}
