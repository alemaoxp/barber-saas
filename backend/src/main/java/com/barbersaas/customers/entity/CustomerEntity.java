package com.barbersaas.customers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome não pode exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 20, message = "Telefone não pode exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String phone;

    @Email(message = "Email deve ser válido")
    @Size(max = 100, message = "Email não pode exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 500, message = "Observações não pode exceder 500 caracteres")
    @Column(length = 500)
    private String notes;

    @NotNull(message = "Status ativo é obrigatório")
    @Column(nullable = false)
    private Boolean active;

    // Constructors
    public CustomerEntity() {
        // Default constructor for JPA
    }

    public CustomerEntity(String name, String phone, String email, LocalDate birthDate, String notes, Boolean active) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.birthDate = birthDate;
        this.notes = notes;
        this.active = active;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

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

    @Override
    public String toString() {
        return "CustomerEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", birthDate=" + birthDate +
                ", notes='" + notes + '\'' +
                ", active=" + active +
                '}';
    }
}