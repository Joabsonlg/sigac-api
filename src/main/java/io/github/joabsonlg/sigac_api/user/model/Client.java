package io.github.joabsonlg.sigac_api.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entity model representing a Client.
 * A Client is a specialization of User who can make reservations.
 */
@Table("client")
public record Client(
    @Id
    @Column("user_cpf")
    String userCpf
) {
    /**
     * Static factory method to create a Client from a User
     */
    public static Client fromUser(User user) {
        return new Client(user.cpf());
    }
}
