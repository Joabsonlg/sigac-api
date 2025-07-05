package io.github.joabsonlg.sigac_api.user.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.user.model.Client;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for executing manual SQL queries related to Client.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class ClientRepository extends BaseRepository<Client, String> {
    
    public ClientRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "client";
    }
    
    @Override
    protected String getIdColumnName() {
        return "user_cpf";
    }
    
    /**
     * Finds all clients
     */
    public Flux<Client> findAll() {
        return databaseClient.sql("SELECT user_cpf FROM client")
                .map((row, metadata) -> new Client(
                        row.get("user_cpf", String.class)
                ))
                .all();
    }
    
    /**
     * Finds client by CPF
     */
    public Mono<Client> findById(String userCpf) {
        return databaseClient.sql("SELECT user_cpf FROM client WHERE user_cpf = :userCpf")
                .bind("userCpf", userCpf)
                .map((row, metadata) -> new Client(
                        row.get("user_cpf", String.class)
                ))
                .one();
    }
    
    /**
     * Finds clients with pagination
     */
    public Flux<Client> findWithPagination(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT c.user_cpf 
                FROM client c 
                INNER JOIN users u ON c.user_cpf = u.cpf 
                ORDER BY u.name 
                LIMIT :limit OFFSET :offset
                """)
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new Client(
                        row.get("user_cpf", String.class)
                ))
                .all();
    }
    
    /**
     * Saves a new client
     */
    public Mono<Client> save(Client client) {
        return databaseClient.sql("INSERT INTO client (user_cpf) VALUES (:userCpf)")
                .bind("userCpf", client.userCpf())
                .then()
                .thenReturn(client);
    }
    
    /**
     * Finds clients with their complete user information
     */
    public Flux<Object[]> findAllWithUserInfo() {
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.address, u.phone 
                FROM client c 
                INNER JOIN users u ON c.user_cpf = u.cpf 
                ORDER BY u.name
                """)
                .map((row, metadata) -> new Object[]{
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                })
                .all();
    }
    
    /**
     * Finds clients with user info using pagination
     */
    public Flux<Object[]> findWithUserInfoPaginated(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.address, u.phone 
                FROM client c 
                INNER JOIN users u ON c.user_cpf = u.cpf 
                ORDER BY u.name 
                LIMIT :limit OFFSET :offset
                """)
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new Object[]{
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                })
                .all();
    }
    
    /**
     * Finds a client with complete user information by CPF
     */
    public Mono<Object[]> findByIdWithUserInfo(String userCpf) {
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.address, u.phone 
                FROM client c 
                INNER JOIN users u ON c.user_cpf = u.cpf 
                WHERE c.user_cpf = :userCpf
                """)
                .bind("userCpf", userCpf)
                .map((row, metadata) -> new Object[]{
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                })
                .one();
    }
    
    /**
     * Gets total count of clients
     */
    public Mono<Long> countAll() {
        return count();
    }
    
    /**
     * Checks if client exists by CPF
     */
    public Mono<Boolean> existsByCpf(String cpf) {
        return existsById(cpf);
    }
    
    /**
     * Deletes client by CPF
     */
    public Mono<Void> deleteByCpf(String cpf) {
        return deleteById(cpf);
    }
}
