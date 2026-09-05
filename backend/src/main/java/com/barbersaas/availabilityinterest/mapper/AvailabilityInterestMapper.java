package com.barbersaas.availabilityinterest.mapper;

import com.barbersaas.availabilityinterest.dto.AvailabilityInterestResponse;
import com.barbersaas.availabilityinterest.entity.AvailabilityInterestEntity;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityInterestMapper {

    public AvailabilityInterestResponse toResponse(
            AvailabilityInterestEntity entity) {

        return new AvailabilityInterestResponse(
                entity.getId(),
                entity.getCustomer().getId(),
                entity.getAppointment().getId(),
                entity.getCreatedAt(),
                entity.getStatus()
        );
    }
}