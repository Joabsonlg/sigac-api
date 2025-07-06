package io.github.joabsonlg.sigac_api.dailyRate.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("daily_rate")
public record DailyRate(
        @Id
        @Column("id")
        Long id,

        @Column("amount")
        Double amount,

        @Column("date_time")
        LocalDateTime dateTime,

        @Column("vehicle_plate")
        String vehiclePlate
) {
    // Aqui usamos vehiclePlate ao invés de Vehicle direto para evitar complexidade
    // no mapeamento relacional simples (você pode criar relacionamento em outra camada)
}