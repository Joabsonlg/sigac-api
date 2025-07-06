package io.github.joabsonlg.sigac_api.common.model;

import io.github.joabsonlg.sigac_api.vehicle.enumeration.VehicleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Vehicle {
    private String plate;
    private String year;
    private String model;
    private String brand;
    private VehicleStatus status;
    private String imageUrl;

    public Vehicle(
            @NotBlank(message = "A placa é obrigatória") @Size(max = 20, message = "A placa deve ter no máximo 20 caracteres") String plate,
            @NotBlank(message = "O ano é obrigatório") @Size(max = 10, message = "O ano deve ter no máximo 10 caracteres") String year,
            @NotBlank(message = "O modelo é obrigatório") @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres") String model,
            @NotBlank(message = "A marca é obrigatória") @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres") String brand,
            @NotBlank(message = "O status é obrigatório") @Size(max = 50, message = "O status deve ter no máximo 50 caracteres") VehicleStatus status,
            @Size(max = 255, message = "A URL da imagem deve ter no máximo 255 caracteres") String imageUrl
    ) {
        this.plate = plate;
        this.year = year;
        this.model = model;
        this.brand = brand;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    public Vehicle() {
        // construtor padrão vazio
    }


    // Getters and setters

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
