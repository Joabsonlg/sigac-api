package io.github.joabsonlg.sigac_api.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entity model representing a User in the system.
 * This is the base user entity that can be extended by Client and Employee.
 */
@Table("users")
public record User(
    @Id
    @Column("cpf")
    String cpf,
    
    @Column("email")
    String email,
    
    @Column("name")
    String name,
    
    @Column("password")
    String password,
    
    @Column("address")
    String address,
    
    @Column("phone")
    String phone
) {
    /**
     * Constructor for creating a new user (without CPF for auto-generation scenarios)
     */
    public User(String email, String name, String password, String address, String phone) {
        this(null, email, name, password, address, phone);
    }
    
    /**
     * Creates a copy of this user with a new password (for password updates)
     */
    public User withPassword(String newPassword) {
        return new User(this.cpf, this.email, this.name, newPassword, this.address, this.phone);
    }
    
    /**
     * Creates a copy of this user with updated information (excluding password)
     */
    public User withUpdatedInfo(String email, String name, String address, String phone) {
        return new User(this.cpf, email, name, this.password, address, phone);
    }
}
