package com.barbersaas.services.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class UpdateServiceRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    private String description;

    @NotNull(message = "Duração é obrigatória")
    @Min(value = 1, message = "Duração deve ser pelo menos 1 minuto")
    @Max(value = 480, message = "Duração não pode exceder 480 minutos")
    private Integer durationMinutes;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
    @Digits(integer = 5, fraction = 2, message = "Preço deve ter até 5 dígitos inteiros e 2 decimais")
    private BigDecimal price;

    @NotNull(message = "Status ativo é obrigatório")
    private Boolean active;

    // Getters and Setters
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
}
