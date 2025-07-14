package io.github.joabsonlg.sigac_api.user.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.user.model.User;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for executing manual SQL queries related to User.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class UserRepository extends BaseRepository<User, String> {
    
    public UserRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "users";
    }
    
    @Override
    protected String getIdColumnName() {
        return "cpf";
    }
    
    /**
     * Finds all users
     */
    public Flux<User> findAll() {
        return databaseClient.sql("SELECT cpf, email, name, password, address, phone FROM users")
                .map((row, metadata) -> new User(
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("password", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                ))
                .all();
    }
    
    /**
     * Finds user by CPF
     */
    public Mono<User> findById(String cpf) {
        return databaseClient.sql("SELECT cpf, email, name, password, address, phone FROM users WHERE cpf = :cpf")
                .bind("cpf", cpf)
                .map((row, metadata) -> new User(
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("password", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                ))
                .one();
    }
    
    /**
     * Finds user by email
     */
    public Mono<User> findByEmail(String email) {
        return databaseClient.sql("SELECT cpf, email, name, password, address, phone FROM users WHERE email = :email")
                .bind("email", email)
                .map((row, metadata) -> new User(
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("password", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                ))
                .one();
    }
    
    /**
     * Finds users with pagination
     */
    public Flux<User> findWithPagination(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("SELECT cpf, email, name, password, address, phone FROM users ORDER BY name LIMIT :limit OFFSET :offset")
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new User(
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("password", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class)
                ))
                .all();
    }
    
    /**
     * Saves a new user
     */
    public Mono<User> save(User user) {
        if (user.cpf() == null) {
            throw new IllegalArgumentException("CPF é obrigatório para criar um usuário");
        }
        
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql("""
                INSERT INTO users (cpf, email, name, password, address, phone) 
                VALUES (:cpf, :email, :name, :password, :address, :phone)
                """)
                .bind("cpf", user.cpf())
                .bind("email", user.email())
                .bind("name", user.name())
                .bind("password", user.password());

        if (user.address() != null) {
            executeSpec = executeSpec.bind("address", user.address());
        } else {
            executeSpec = executeSpec.bindNull("address", String.class);
        }

        if (user.phone() != null) {
            executeSpec = executeSpec.bind("phone", user.phone());
        } else {
            executeSpec = executeSpec.bindNull("phone", String.class);
        }

        return executeSpec.then().thenReturn(user);
    }
    
    /**
     * Updates an existing user
     */
    public Mono<User> update(User user) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql("""
                UPDATE users 
                SET email = :email, name = :name, password = :password, address = :address, phone = :phone
                WHERE cpf = :cpf
                """)
                .bind("cpf", user.cpf())
                .bind("email", user.email())
                .bind("name", user.name())
                .bind("password", user.password());

        if (user.address() != null) {
            executeSpec = executeSpec.bind("address", user.address());
        } else {
            executeSpec = executeSpec.bindNull("address", String.class);
        }

        if (user.phone() != null) {
            executeSpec = executeSpec.bind("phone", user.phone());
        } else {
            executeSpec = executeSpec.bindNull("phone", String.class);
        }

        return executeSpec.then().thenReturn(user);
    }
    
    /**
     * Updates user password
     */
    public Mono<Void> updatePassword(String cpf, String newPassword) {
        return databaseClient.sql("UPDATE users SET password = :password WHERE cpf = :cpf")
                .bind("cpf", cpf)
                .bind("password", newPassword)
                .then();
    }
    
    /**
     * Checks if email already exists for a different user
     */
    public Mono<Boolean> existsByEmailAndCpfNot(String email, String cpf) {
        return databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email AND cpf != :cpf")
                .bind("email", email)
                .bind("cpf", cpf)
                .map((row, metadata) -> {
                    Long count = row.get(0, Long.class);
                    return count != null && count > 0;
                })
                .one();
    }
    
    /**
     * Checks if email already exists
     */
    public Mono<Boolean> existsByEmail(String email) {
        return databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                .bind("email", email)
                .map((row, metadata) -> {
                    Long count = row.get(0, Long.class);
                    return count != null && count > 0;
                })
                .one();
    }
    
    /**
     * Gets total count of users
     */
    public Mono<Long> countAll() {
        return count();
    }
    
    /**
     * Checks if user exists by CPF
     */
    public Mono<Boolean> existsByCpf(String cpf) {
        return existsById(cpf);
    }
    
    /**
     * Deletes user by CPF
     */
    public Mono<Void> deleteByCpf(String cpf) {
        return deleteById(cpf);
    }
}