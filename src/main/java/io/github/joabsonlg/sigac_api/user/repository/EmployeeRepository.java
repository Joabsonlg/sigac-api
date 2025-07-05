package io.github.joabsonlg.sigac_api.user.repository;

import io.github.joabsonlg.sigac_api.common.base.BaseRepository;
import io.github.joabsonlg.sigac_api.user.model.Employee;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository for executing manual SQL queries related to Employee.
 * Extends BaseRepository for common database operations.
 */
@Repository
public class EmployeeRepository extends BaseRepository<Employee, String> {
    
    public EmployeeRepository(DatabaseClient databaseClient) {
        super(databaseClient);
    }
    
    @Override
    protected String getTableName() {
        return "employee";
    }
    
    @Override
    protected String getIdColumnName() {
        return "user_cpf";
    }
    
    /**
     * Finds all employees
     */
    public Flux<Employee> findAll() {
        return databaseClient.sql("SELECT user_cpf, role FROM employee")
                .map((row, metadata) -> new Employee(
                        row.get("user_cpf", String.class),
                        row.get("role", String.class)
                ))
                .all();
    }
    
    /**
     * Finds employee by CPF
     */
    public Mono<Employee> findById(String userCpf) {
        return databaseClient.sql("SELECT user_cpf, role FROM employee WHERE user_cpf = :userCpf")
                .bind("userCpf", userCpf)
                .map((row, metadata) -> new Employee(
                        row.get("user_cpf", String.class),
                        row.get("role", String.class)
                ))
                .one();
    }
    
    /**
     * Finds employees with pagination
     */
    public Flux<Employee> findWithPagination(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT e.user_cpf, e.role 
                FROM employee e 
                INNER JOIN users u ON e.user_cpf = u.cpf 
                ORDER BY u.name 
                LIMIT :limit OFFSET :offset
                """)
                .bind("limit", size)
                .bind("offset", offset)
                .map((row, metadata) -> new Employee(
                        row.get("user_cpf", String.class),
                        row.get("role", String.class)
                ))
                .all();
    }
    
    /**
     * Saves a new employee
     */
    public Mono<Employee> save(Employee employee) {
        return databaseClient.sql("INSERT INTO employee (user_cpf, role) VALUES (:userCpf, :role)")
                .bind("userCpf", employee.userCpf())
                .bind("role", employee.role())
                .then()
                .thenReturn(employee);
    }
    
    /**
     * Updates an existing employee
     */
    public Mono<Employee> update(Employee employee) {
        return databaseClient.sql("UPDATE employee SET role = :role WHERE user_cpf = :userCpf")
                .bind("userCpf", employee.userCpf())
                .bind("role", employee.role())
                .then()
                .thenReturn(employee);
    }
    
    /**
     * Finds employees with their complete user information
     */
    public Flux<Object[]> findAllWithUserInfo() {
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.address, u.phone, e.role 
                FROM employee e 
                INNER JOIN users u ON e.user_cpf = u.cpf 
                ORDER BY u.name
                """)
                .map((row, metadata) -> new Object[]{
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class),
                        row.get("role", String.class)
                })
                .all();
    }
    
    /**
     * Finds employees with user info using pagination
     */
    public Flux<Object[]> findWithUserInfoPaginated(int page, int size) {
        int offset = page * size;
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.address, u.phone, e.role 
                FROM employee e 
                INNER JOIN users u ON e.user_cpf = u.cpf 
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
                        row.get("phone", String.class),
                        row.get("role", String.class)
                })
                .all();
    }
    
    /**
     * Finds an employee with complete user information by CPF
     */
    public Mono<Object[]> findByIdWithUserInfo(String userCpf) {
        return databaseClient.sql("""
                SELECT u.cpf, u.email, u.name, u.address, u.phone, e.role 
                FROM employee e 
                INNER JOIN users u ON e.user_cpf = u.cpf 
                WHERE e.user_cpf = :userCpf
                """)
                .bind("userCpf", userCpf)
                .map((row, metadata) -> new Object[]{
                        row.get("cpf", String.class),
                        row.get("email", String.class),
                        row.get("name", String.class),
                        row.get("address", String.class),
                        row.get("phone", String.class),
                        row.get("role", String.class)
                })
                .one();
    }
    
    /**
     * Finds employees by role
     */
    public Flux<Employee> findByRole(String role) {
        return databaseClient.sql("SELECT user_cpf, role FROM employee WHERE role = :role")
                .bind("role", role)
                .map((row, metadata) -> new Employee(
                        row.get("user_cpf", String.class),
                        row.get("role", String.class)
                ))
                .all();
    }
    
    /**
     * Gets total count of employees
     */
    public Mono<Long> countAll() {
        return count();
    }
    
    /**
     * Checks if employee exists by CPF
     */
    public Mono<Boolean> existsByCpf(String cpf) {
        return existsById(cpf);
    }
    
    /**
     * Deletes employee by CPF
     */
    public Mono<Void> deleteByCpf(String cpf) {
        return deleteById(cpf);
    }
}
