package com.barbersaas.auth.entity;

import com.barbersaas.barbershops.entity.BarbershopEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AdminUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private BarbershopEntity barbershop;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String passwordHash;

    @NotNull
    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminUserEntity() {
    }

    public AdminUserEntity(BarbershopEntity barbershop, String name, String email, String passwordHash, Boolean active) {
        this.barbershop = barbershop;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    public UUID getId() { return id; }
    public BarbershopEntity getBarbershop() { return barbershop; }
    public void setBarbershop(BarbershopEntity barbershop) { this.barbershop = barbershop; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
