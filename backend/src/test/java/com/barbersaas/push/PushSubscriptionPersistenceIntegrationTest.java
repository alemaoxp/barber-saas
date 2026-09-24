package com.barbersaas.push;

import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.entity.PushSubscriptionEntity;
import com.barbersaas.push.repository.PushSubscriptionRepository;
import com.barbersaas.push.service.PushProperties;
import com.barbersaas.push.service.WebPushService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PushSubscriptionPersistenceIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private PushSubscriptionRepository subscriptions;

    private UUID customerId;
    private UUID secondCustomerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        secondCustomerId = UUID.randomUUID();
        insertCustomer(customerId, "Push Persistência A");
        insertCustomer(secondCustomerId, "Push Persistência B");
    }

    @AfterEach
    void tearDown() {
        jdbc.update("DELETE FROM push_subscriptions WHERE customer_id = ?", customerId);
        jdbc.update("DELETE FROM push_subscriptions WHERE customer_id = ?", secondCustomerId);
        jdbc.update("DELETE FROM customers WHERE id = ?", customerId);
        jdbc.update("DELETE FROM customers WHERE id = ?", secondCustomerId);
    }

    @Test
    void postgresMigrationAndRepositorySurviveAServiceRestart() throws Exception {
        assertEquals("push_subscriptions", jdbc.queryForObject(
                "SELECT to_regclass('public.push_subscriptions')", String.class));
        assertEquals("ux_push_subscriptions_endpoint", jdbc.queryForObject(
                "SELECT to_regclass('public.ux_push_subscriptions_endpoint')", String.class));
        assertEquals("ix_push_subscriptions_customer_id", jdbc.queryForObject(
                "SELECT to_regclass('public.ix_push_subscriptions_customer_id')", String.class));
        assertEquals(1, jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM pg_constraint
                WHERE conrelid = 'public.push_subscriptions'::regclass
                  AND contype = 'f'
                """, Integer.class));

        PushProperties properties = enabledProperties();
        properties.setEnabled(true);
        WebPushService firstService = new WebPushService(properties, subscriptions, customers);
        firstService.save(new PushSubscriptionRequest(
                "https://push.example.test/restart", "key-v1", "auth-v1", customerId));
        LocalDateTime firstUpdatedAt = jdbc.queryForObject(
                "SELECT updated_at FROM push_subscriptions WHERE endpoint = ?",
                LocalDateTime.class, "https://push.example.test/restart");

        WebPushService restartedService = new WebPushService(properties, subscriptions, customers);
        Thread.sleep(10);
        restartedService.save(new PushSubscriptionRequest(
                "https://push.example.test/restart", "key-v2", "auth-v2", customerId));
        PushSubscriptionEntity persisted = subscriptions.findAllByCustomerId(customerId)
                .stream().findFirst().orElseThrow();

        assertNotNull(persisted.getId());
        assertEquals("key-v2", persisted.getP256dh());
        assertEquals("auth-v2", persisted.getAuth());
        LocalDateTime secondUpdatedAt = jdbc.queryForObject(
                "SELECT updated_at FROM push_subscriptions WHERE endpoint = ?",
                LocalDateTime.class, "https://push.example.test/restart");
        assertTrue(secondUpdatedAt.isAfter(firstUpdatedAt));
        assertEquals(1, subscriptions.findAllByCustomerId(customerId).size());
    }

    @Test
    void concurrentRegistrationForSameCustomerIsIdempotent() throws Exception {
        String endpoint = "https://push.example.test/concurrent-same-customer-" + UUID.randomUUID();
        WebPushService service = enabledService();
        List<Throwable> failures = runConcurrently(
                () -> service.save(new PushSubscriptionRequest(
                        endpoint, "key-a", "auth-a", customerId)),
                () -> service.save(new PushSubscriptionRequest(
                        endpoint, "key-b", "auth-b", customerId)));

        assertTrue(failures.stream().allMatch(failure -> failure == null), failures.toString());
        assertEquals(1, countSubscriptions(endpoint));
        assertEquals(customerId, jdbc.queryForObject(
                "SELECT customer_id FROM push_subscriptions WHERE endpoint = ?",
                UUID.class, endpoint));
        PushSubscriptionEntity persisted = subscriptions.findByEndpoint(endpoint).orElseThrow();
        assertTrue(Set.of("key-a:auth-a", "key-b:auth-b")
                .contains(persisted.getP256dh() + ":" + persisted.getAuth()));
    }

    @Test
    void concurrentRegistrationForDifferentCustomersRejectsOnlyTheLoser() throws Exception {
        String endpoint = "https://push.example.test/concurrent-different-customers-" + UUID.randomUUID();
        WebPushService service = enabledService();
        List<Throwable> failures = runConcurrently(
                () -> service.save(new PushSubscriptionRequest(
                        endpoint, "key-a", "auth-a", customerId)),
                () -> service.save(new PushSubscriptionRequest(
                        endpoint, "key-b", "auth-b", secondCustomerId)));

        assertEquals(1, failures.stream().filter(failure -> failure == null).count());
        List<Throwable> errors = failures.stream().filter(failure -> failure != null).toList();
        assertEquals(1, errors.size());
        assertInstanceOf(com.barbersaas.exception.BusinessException.class, errors.get(0));
        assertFalse(errors.get(0) instanceof org.springframework.dao.DataIntegrityViolationException);
        assertEquals(1, countSubscriptions(endpoint));
        UUID owner = jdbc.queryForObject(
                "SELECT customer_id FROM push_subscriptions WHERE endpoint = ?",
                UUID.class, endpoint);
        assertTrue(Set.of(customerId, secondCustomerId).contains(owner));
    }

    private WebPushService enabledService() {
        return new WebPushService(enabledProperties(), subscriptions, customers);
    }

    private PushProperties enabledProperties() {
        PushProperties properties = new PushProperties();
        properties.setEnabled(true);
        properties.setVapidPublicKey("public");
        properties.setVapidPrivateKey("private");
        properties.setVapidSubject("mailto:test@example.com");
        return properties;
    }

    private List<Throwable> runConcurrently(ThrowingRunnable... operations)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(operations.length);
        CountDownLatch ready = new CountDownLatch(operations.length);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Throwable>> futures = new ArrayList<>();
            for (ThrowingRunnable operation : operations) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        operation.run();
                        return null;
                    } catch (Throwable failure) {
                        return failure;
                    }
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            List<Throwable> failures = new ArrayList<>();
            for (Future<Throwable> future : futures) {
                failures.add(future.get(10, TimeUnit.SECONDS));
            }
            return failures;
        } finally {
            executor.shutdownNow();
        }
    }

    private int countSubscriptions(String endpoint) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM push_subscriptions WHERE endpoint = ?",
                Integer.class, endpoint);
    }

    private void insertCustomer(UUID id, String name) {
        UUID barbershopId = jdbc.queryForObject(
                "SELECT id FROM barbershops ORDER BY id LIMIT 1", UUID.class);
        jdbc.update(
                "INSERT INTO customers (id, name, phone, active, barbershop_id) VALUES (?, ?, ?, ?, ?)",
                id, name, "(11) 90000-" + id.toString().substring(0, 4), true, barbershopId);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
