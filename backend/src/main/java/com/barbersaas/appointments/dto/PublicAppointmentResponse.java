package com.barbersaas.appointments.dto;

import com.barbersaas.appointments.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PublicAppointmentResponse extends AppointmentResponse {
    private String cancelToken;

    public PublicAppointmentResponse() {}

    public PublicAppointmentResponse(UUID id, UUID customerId, List<UUID> serviceIds, BigDecimal totalPrice,
                                     LocalDateTime appointmentDateTime, AppointmentStatus status,
                                     String notes, LocalDateTime createdAt, String cancelToken) {
        super(id, customerId, serviceIds, totalPrice, appointmentDateTime, status, notes, createdAt);
        this.cancelToken = cancelToken;
    }

    public String getCancelToken() { return cancelToken; }
    public void setCancelToken(String cancelToken) { this.cancelToken = cancelToken; }
}
