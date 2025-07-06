package io.github.joabsonlg.sigac_api.vehicle.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehicle")
public record Vehicle(
        @Id
        @Column("plate")
        String plate,

        @Column("year")
        Integer year,

        @Column("model")
        String model,

        @Column("brand")
        String brand,

        @Column("status")
        String status,

        @Column("image_url")
        String imageUrl
) {
    // Você pode criar construtores auxiliares, se quiser,
    // ou métodos para alterar algum campo retornando um novo record,
    // no estilo do seu User com withUpdatedInfo, se precisar.
}