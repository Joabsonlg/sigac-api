package io.github.joabsonlg.sigac_api.auth.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.common.model.User;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repository for authentication-related database operations.
 * Extends BaseRepository for common operations and adds auth-specific queries.
 */
@Repository
public class AuthRepository extends BaseRepository<User, String> {
    
    public AuthRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "users";
    }
    
    /**
     * Finds user by CPF for authentication.
     *
     * @param cpf user's CPF
     * @return user entity if found
     */
    public Mono<User> findByCpf(String cpf) {
        return databaseClient.sql("SELECT cpf, email, name, password, address, phone FROM users WHERE cpf = $1")
                .bind("$1", cpf)
                .map((row, metadata) -> {
                    User user = new User();
                    user.setCpf(row.get("cpf", String.class));
                    user.setEmail(row.get("email", String.class));
                    user.setName(row.get("name", String.class));
                    user.setPassword(row.get("password", String.class));
                    user.setAddress(row.get("address", String.class));
                    user.setPhone(row.get("phone", String.class));
                    return user;
                })
                .one();
    }
    
    /**
     * Finds user with role information by CPF.
     *
     * @param cpf user's CPF
     * @return user with role information
     */
    public Mono<UserWithRole> findUserWithRoleByCpf(String cpf) {
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.password, u.address, u.phone,
                       COALESCE(e.role, 'CLIENT') as role
                FROM users u
                LEFT JOIN employee e ON u.cpf = e.user_cpf
                WHERE u.cpf = $1
                """)
                .bind("$1", cpf)
                .map((row, metadata) -> {
                    User user = new User();
                    user.setCpf(row.get("cpf", String.class));
                    user.setEmail(row.get("email", String.class));
                    user.setName(row.get("name", String.class));
                    user.setPassword(row.get("password", String.class));
                    user.setAddress(row.get("address", String.class));
                    user.setPhone(row.get("phone", String.class));
                    
                    String role = row.get("role", String.class);
                    return new UserWithRole(user, role);
                })
                .one();
    }
    
    /**
     * Checks if user exists by CPF.
     *
     * @param cpf user's CPF
     * @return true if user exists, false otherwise
     */
    public Mono<Boolean> existsByCpf(String cpf) {
        return databaseClient.sql("SELECT COUNT(*) FROM users WHERE cpf = $1")
                .bind("$1", cpf)
                .map((row, metadata) -> row.get(0, Long.class) > 0)
                .one();
    }
    
    /**
     * Checks if email is already in use by another user.
     *
     * @param email email to check
     * @param excludeCpf CPF to exclude from check (for updates)
     * @return true if email is in use, false otherwise
     */
    public Mono<Boolean> isEmailInUse(String email, String excludeCpf) {
        return databaseClient.sql("SELECT COUNT(*) FROM users WHERE email = $1 AND cpf != $2")
                .bind("$1", email)
                .bind("$2", excludeCpf)
                .map((row, metadata) -> row.get(0, Long.class) > 0)
                .one();
    }
    
    /**
     * Updates user's password.
     *
     * @param cpf         user's CPF
     * @param newPassword new encoded password
     * @return completion signal
     */
    public Mono<Void> updatePassword(String cpf, String newPassword) {
        return databaseClient.sql("UPDATE users SET password = $1 WHERE cpf = $2")
                .bind("$1", newPassword)
                .bind("$2", cpf)
                .then();
    }
    
    /**
     * Record for user with role information.
     */
    public record UserWithRole(User user, String role) {}
}
