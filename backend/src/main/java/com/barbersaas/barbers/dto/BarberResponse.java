package com.barbersaas.barbers.dto;

import java.util.UUID;

public class BarberResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String specialties;
    private Boolean active;

    // Constructors
    public BarberResponse() {
    }

    public BarberResponse(UUID id, String name, String email, String phone, String specialties, Boolean active) {
        this.id = id;
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

    public void setId(UUID id) {
        this.id = id;
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
}