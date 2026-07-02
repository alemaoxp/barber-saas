package com.barbersaas.services.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Duração é obrigatória")
    @Min(value = 1, message = "Duração deve ser pelo menos 1 minuto")
    @Max(value = 480, message = "Duração não pode exceder 480 minutos")
    @Column(nullable = false)
    private Integer durationMinutes;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
    @Digits(integer = 5, fraction = 2, message = "Preço deve ter até 5 dígitos inteiros e 2 decimais")
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Status ativo é obrigatório")
    @Column(nullable = false)
    private Boolean active;

    // Constructors
    public ServiceEntity() {
        // Default constructor for JPA
    }

    public ServiceEntity(String name, String description, Integer durationMinutes, BigDecimal price, Boolean active) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ServiceEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", durationMinutes=" + durationMinutes +
                ", price=" + price +
                ", active=" + active +
                '}';
    }
}