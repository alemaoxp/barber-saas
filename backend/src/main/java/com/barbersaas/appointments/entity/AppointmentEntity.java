package com.barbersaas.appointments.entity;

import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.barbers.entity.BarberEntity;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.services.entity.ServiceEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id", nullable = false) private CustomerEntity customer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "barber_id", nullable = false) private BarberEntity barber;
    @ManyToMany
    @JoinTable(name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<ServiceEntity> services = new ArrayList<>();
    @Column(name = "total_price", precision = 7, scale = 2) private BigDecimal totalPrice;
    @Column(name = "appointment_date_time", nullable = false) private LocalDateTime appointmentDateTime;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false) private AppointmentStatus status;
    @Column(name = "notes", length = 255) private String notes;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "cancel_token", length = 64, unique = true) private String cancelToken;

    public AppointmentEntity() {}

    public AppointmentEntity(CustomerEntity customer, BarberEntity barber, List<ServiceEntity> services,
                             BigDecimal totalPrice, LocalDateTime appointmentDateTime,
                             AppointmentStatus status, String notes) {
        this.customer = customer;
        this.barber = barber;
        this.services = new ArrayList<>(services);
        this.totalPrice = totalPrice;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public CustomerEntity getCustomer() { return customer; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }
    public BarberEntity getBarber() { return barber; }
    public void setBarber(BarberEntity barber) { this.barber = barber; }
    public List<ServiceEntity> getServices() { return services; }
    public void setServices(List<ServiceEntity> services) { this.services = new ArrayList<>(services); }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCancelToken() { return cancelToken; }
    public void setCancelToken(String cancelToken) { this.cancelToken = cancelToken; }
}
