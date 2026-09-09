package com.barbersaas.customers.dto;

import java.util.UUID;

public class CustomerSummaryResponse {

    private UUID id;
    private String name;
    private String phone;

    public CustomerSummaryResponse() {
    }

    public CustomerSummaryResponse(UUID id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

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
}
