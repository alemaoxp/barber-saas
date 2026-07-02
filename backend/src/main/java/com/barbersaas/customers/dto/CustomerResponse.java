package com.barbersaas.customers.dto;

import java.time.LocalDate;
import java.util.UUID;

public class CustomerResponse {

    private UUID id;
    private String name;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private String notes;
    private Boolean active;

    // Constructors
    public CustomerResponse() {
    }

    public CustomerResponse(UUID id, String name, String phone, String email, LocalDate birthDate, String notes, Boolean active) {
        this.id = id;
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

    public void setId(UUID id) {
        this.id = id;
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
}