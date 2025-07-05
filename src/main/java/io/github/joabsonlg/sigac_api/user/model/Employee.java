package io.github.joabsonlg.sigac_api.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entity model representing an Employee.
 * An Employee is a specialization of User who can manage the system.
 */
@Table("employee")
public record Employee(
    @Id
    @Column("user_cpf")
    String userCpf,
    
    @Column("role")
    String role
) {
    /**
     * Static factory method to create an Employee from a User
     */
    public static Employee fromUser(User user, String role) {
        return new Employee(user.cpf(), role);
    }
    
    /**
     * Creates a copy of this employee with a new role
     */
    public Employee withRole(String newRole) {
        return new Employee(this.userCpf, newRole);
    }
}
