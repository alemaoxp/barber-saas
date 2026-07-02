package com.barbersaas.customers.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class UpdateCustomerRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode exceder 100 caracteres")
    private String name;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
    private String phone;

    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    private String email;

    private LocalDate birthDate;

    @Size(max = 500, message = "Observações não pode exceder 500 caracteres")
    private String notes;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean active;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}