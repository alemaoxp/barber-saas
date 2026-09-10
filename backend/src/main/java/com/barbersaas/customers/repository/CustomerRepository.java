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
    java.util.Optional<CustomerEntity> findByBarbershopIdAndPhone(UUID barbershopId, String phone);

    @Query("""
            SELECT DISTINCT c
            FROM CustomerEntity c
            WHERE c.barbershop.id = :barbershopId
            """)
    List<CustomerEntity> findDistinctByBarberId(
            @Param("barbershopId") UUID barbershopId
    );

    @Query("""
            SELECT DISTINCT c
            FROM CustomerEntity c
            WHERE c.barbershop.id = :barbershopId
            AND (
                LOWER(c.name) LIKE LOWER(:queryPattern)
                OR c.phone LIKE :queryPattern
            )
            """)
    List<CustomerEntity> findDistinctByBarberIdAndQuery(
            @Param("barbershopId") UUID barbershopId,
            @Param("queryPattern") String queryPattern
    );
}
