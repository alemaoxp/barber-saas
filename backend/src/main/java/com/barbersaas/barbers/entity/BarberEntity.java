package com.barbersaas.barbers.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Entity
@Table(name = "barbers")
public class BarberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^\\(?[1-9]{2}\\)? ?(?:[2-8]|9[1-9])[0-9]{3}\\-?[0-9]{4}$", 
             message = "Telefone deve seguir o formato brasileiro (ex: (11) 99999-9999)")
    @Column(nullable = false, length = 20)
    private String phone;

    @Size(max = 500, message = "Especialidades não pode exceder 500 caracteres")
    @Column(length = 500)
    private String specialties;

    @NotNull(message = "Status ativo é obrigatório")
    @Column(nullable = false)
    private Boolean active;

    // Constructors
    public BarberEntity() {
        // Default constructor for JPA
    }

    public BarberEntity(String name, String email, String phone, String specialties, Boolean active) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.specialties = specialties;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "BarberEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", specialties='" + specialties + '\'' +
                ", active=" + active +
                '}';
    }
}