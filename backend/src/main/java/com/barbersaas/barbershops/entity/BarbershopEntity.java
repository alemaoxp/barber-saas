package com.barbersaas.barbershops.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "barbershops")
public class BarbershopEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean active;

    public BarbershopEntity() { }

    public BarbershopEntity(UUID id, String name, Boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Boolean getActive() { return active; }
}
