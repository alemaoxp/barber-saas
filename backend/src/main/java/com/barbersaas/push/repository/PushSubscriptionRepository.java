package com.barbersaas.push.repository;

import com.barbersaas.push.entity.PushSubscriptionEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscriptionEntity, UUID> {

    Optional<PushSubscriptionEntity> findByEndpoint(String endpoint);

    List<PushSubscriptionEntity> findAllByCustomerId(UUID customerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            INSERT INTO push_subscriptions
                (id, customer_id, endpoint, p256dh, auth, created_at, updated_at)
            VALUES
                (:id, :customerId, :endpoint, :p256dh, :auth, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (endpoint) DO UPDATE
            SET p256dh = EXCLUDED.p256dh,
                auth = EXCLUDED.auth,
                updated_at = CURRENT_TIMESTAMP
            WHERE push_subscriptions.customer_id = EXCLUDED.customer_id
            """, nativeQuery = true)
    int upsertForCustomer(
            @Param("id") UUID id,
            @Param("customerId") UUID customerId,
            @Param("endpoint") String endpoint,
            @Param("p256dh") String p256dh,
            @Param("auth") String auth);
}
