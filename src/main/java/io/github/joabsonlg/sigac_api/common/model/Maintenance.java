package io.github.joabsonlg.sigac_api.common.model;

import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceStatus;
import io.github.joabsonlg.sigac_api.maintenance.enumeration.MaintenanceType;

import java.time.LocalDateTime;

public class Maintenance {
    private Long id;
    private LocalDateTime scheduledDate;
    private LocalDateTime performedDate;
    private String description;
    private MaintenanceType type;
    private MaintenanceStatus status;
    private String cost;
    private String employeeUserCpf;
    private Vehicle vehicle;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalDateTime getPerformedDate() {
        return performedDate;
    }

    public void setPerformedDate(LocalDateTime performedDate) {
        this.performedDate = performedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MaintenanceType getType() {
        return type;
    }

    public void setType(MaintenanceType type) {
        this.type = type;
    }

    public MaintenanceStatus  getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getEmployeeUserCpf() {
        return employeeUserCpf;
    }

    public void setEmployeeUserCpf(String employeeUserCpf) {
        this.employeeUserCpf = employeeUserCpf;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}
