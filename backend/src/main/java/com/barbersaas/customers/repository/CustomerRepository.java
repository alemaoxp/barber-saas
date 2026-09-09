package com.barbersaas.customers.repository;

import com.barbersaas.customers.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    java.util.Optional<CustomerEntity> findByPhone(String phone);

    @Query("""
            SELECT DISTINCT c
            FROM AppointmentEntity a
            JOIN a.customer c
            WHERE a.barber.id = :barberId
            """)
    List<CustomerEntity> findDistinctByBarberId(
            @Param("barberId") UUID barberId
    );

    @Query("""
            SELECT DISTINCT c
            FROM AppointmentEntity a
            JOIN a.customer c
            WHERE a.barber.id = :barberId
            AND (
                LOWER(c.name) LIKE LOWER(:queryPattern)
                OR c.phone LIKE :queryPattern
            )
            """)
    List<CustomerEntity> findDistinctByBarberIdAndQuery(
            @Param("barberId") UUID barberId,
            @Param("queryPattern") String queryPattern
    );
}
