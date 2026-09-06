package com.barbersaas.appointments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CreatePublicAppointmentRequest {
    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String customerName;
    @NotBlank(message = "Telefone do cliente é obrigatório")
    @Pattern(regexp = "^\\(?[1-9]{2}\\)? ?(?:[2-8]|9[1-9])[0-9]{3}\\-?[0-9]{4}$",
            message = "Telefone deve seguir o formato brasileiro (ex: (11) 99999-9999)")
    private String customerPhone;
    @NotEmpty(message = "Serviços são obrigatórios")
    private List<@NotNull UUID> serviceIds;
    @NotNull(message = "Data e horário são obrigatórios")
    private LocalDateTime appointmentDateTime;
    @Size(max = 255, message = "Observações não pode exceder 255 caracteres")
    private String notes;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public List<UUID> getServiceIds() { return serviceIds; }
    public void setServiceIds(List<UUID> serviceIds) { this.serviceIds = serviceIds; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
