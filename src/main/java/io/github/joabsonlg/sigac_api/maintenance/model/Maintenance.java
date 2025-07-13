package io.github.joabsonlg.sigac_api.maintenance.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("maintenance")
public record Maintenance(
        @Id
        @Column("id")
        Long id,

        @Column("scheduled_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime scheduledDate,

        @Column("performed_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime performedDate,

        @Column("description")
        String description,

        @Column("type")
        MaintenanceType type,

        @Column("status")
        MaintenanceStatus status,

        @Column("cost")
        String cost,

        @Column("employee_user_cpf")
        String employeeUserCpf,

        @Column("vehicle_plate")
        String vehiclePlate
) {
    // Caso queira, pode adicionar métodos auxiliares como withUpdatedInfo
}