package io.github.joabsonlg.sigac_api.user.handler;

import io.github.joabsonlg.sigac_api.common.base.BaseHandler;
import io.github.joabsonlg.sigac_api.common.exception.ConflictException;
import io.github.joabsonlg.sigac_api.common.exception.ResourceNotFoundException;
import io.github.joabsonlg.sigac_api.common.response.PageResponse;
import io.github.joabsonlg.sigac_api.user.dto.ClientDTO;
import io.github.joabsonlg.sigac_api.user.dto.CreateUserDTO;
import io.github.joabsonlg.sigac_api.user.model.Client;
import io.github.joabsonlg.sigac_api.user.repository.ClientRepository;
import io.github.joabsonlg.sigac_api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Handler for business logic related to Client.
 * Extends BaseHandler for DTO/Entity conversions.
 */
@Service
public class ClientHandler extends BaseHandler<Client, ClientDTO, String> {
    
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final UserHandler userHandler;
    
    public ClientHandler(ClientRepository clientRepository, UserRepository userRepository, UserHandler userHandler) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.userHandler = userHandler;
    }
    
    @Override
    protected ClientDTO toDto(Client entity) {
        // This method won't be used directly since we need user info
        return new ClientDTO(
            entity.userCpf(),
            null, null, null, null // Will be filled by the methods that fetch user info
        );
    }
    
    @Override
    protected Client toEntity(ClientDTO dto) {
        return new Client(dto.cpf());
    }
    
    /**
     * Converts array of user info to ClientDTO
     */
    private ClientDTO arrayToClientDto(Object[] userInfo) {
        return new ClientDTO(
            (String) userInfo[0], // cpf
            (String) userInfo[1], // email
            (String) userInfo[2], // name
            (String) userInfo[3], // address
            (String) userInfo[4]  // phone
        );
    }
    
    /**
     * Gets all clients with complete user information
     */
    public Flux<ClientDTO> getAll() {
        return clientRepository.findAllWithUserInfo()
                .map(this::arrayToClientDto);
    }
    
    /**
     * Gets client by CPF with complete user information
     */
    public Mono<ClientDTO> getById(String cpf) {
        return clientRepository.findByIdWithUserInfo(cpf)
                .map(this::arrayToClientDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Cliente", cpf)));
    }
    
    /**
     * Gets paginated clients with complete user information
     */
    public Mono<PageResponse<ClientDTO>> getAllPaginated(int page, int size) {
        Flux<ClientDTO> clients = clientRepository.findWithUserInfoPaginated(page, size)
                .map(this::arrayToClientDto);
        
        // Count total clients
        Mono<Long> totalElements = clientRepository.countAll();
        
        return createPageResponse(clients, page, size, totalElements);
    }
    
    /**
     * Creates a new client (creates user first, then client record)
     */
    public Mono<ClientDTO> create(CreateUserDTO createUserDTO) {
        return userHandler.create(createUserDTO)
                .flatMap(userDTO -> {
                    Client client = new Client(userDTO.cpf());
                    return clientRepository.save(client)
                            .then(Mono.just(ClientDTO.fromUserDTO(userDTO)));
                });
    }
    
    /**
     * Converts an existing user to a client
     */
    public Mono<ClientDTO> convertUserToClient(String cpf) {
        return userRepository.findById(cpf)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuário", cpf)))
                .flatMap(user -> clientRepository.findById(cpf)
                        .hasElement()
                        .flatMap(isClient -> {
                            if (isClient) {
                                return Mono.error(new ConflictException("Usuário já é um cliente"));
                            }
                            Client client = Client.fromUser(user);
                            return clientRepository.save(client)
                                    .then(userHandler.getById(cpf))
                                    .map(ClientDTO::fromUserDTO);
                        }));
    }
    
    /**
     * Deletes a client (removes client record but keeps user)
     */
    public Mono<Void> delete(String cpf) {
        return clientRepository.existsByCpf(cpf)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Cliente", cpf));
                    }
                    return clientRepository.deleteByCpf(cpf);
                });
    }
    
    /**
     * Deletes a client and the associated user
     */
    public Mono<Void> deleteWithUser(String cpf) {
        return clientRepository.existsByCpf(cpf)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Cliente", cpf));
                    }
                    return clientRepository.deleteByCpf(cpf)
                            .then(userHandler.delete(cpf));
                });
    }
    
    /**
     * Checks if user is a client
     */
    public Mono<Boolean> isClient(String cpf) {
        return clientRepository.existsByCpf(cpf);
    }
}
