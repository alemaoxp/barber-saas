package com.barbersaas.appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class CreatePublicAppointmentRequest {

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String customerName;

    @NotBlank(message = "Telefone do cliente é obrigatório")
    @Pattern(regexp = "^\\(?[1-9]{2}\\)? ?(?:[2-8]|9[1-9])[0-9]{3}\\-?[0-9]{4}$", 
             message = "Telefone deve seguir o formato brasileiro (ex: (11) 99999-9999)")
    private String customerPhone;

    @NotNull(message = "Serviço é obrigatório")
    private UUID serviceId;

    @NotNull(message = "Data e horário são obrigatórios")
    private LocalDateTime appointmentDateTime;

    @Size(max = 255, message = "Observações não pode exceder 255 caracteres")
    private String notes;

    public CreatePublicAppointmentRequest() {
    }

    public CreatePublicAppointmentRequest(String customerName, String customerPhone, UUID serviceId, 
                                         LocalDateTime appointmentDateTime, String notes) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.serviceId = serviceId;
        this.appointmentDateTime = appointmentDateTime;
        this.notes = notes;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreatePublicAppointmentRequest{" +
                "customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", serviceId=" + serviceId +
                ", appointmentDateTime=" + appointmentDateTime +
                ", notes='" + notes + '\'' +
                '}';
    }
}