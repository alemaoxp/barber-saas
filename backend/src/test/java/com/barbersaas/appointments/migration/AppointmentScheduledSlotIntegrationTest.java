package com.barbersaas.appointments.migration;

import com.barbersaas.appointments.entity.AppointmentEntity;
import com.barbersaas.appointments.enums.AppointmentStatus;
import com.barbersaas.appointments.service.AppointmentService;
import com.barbersaas.barbers.repository.BarberRepository;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AppointmentScheduledSlotIntegrationTest {
    private static final UUID BARBER_ID = UUID.fromString(
            "3700633c-35f1-4ab9-af18-c60f8eb23b45"
    );

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private BarberRepository barberRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private UUID customerId;
    private UUID barbershopId;

    @BeforeEach
    void setUp() {
        barbershopId = jdbc.queryForObject(
                "SELECT barbershop_id FROM barbers WHERE id = ?",
                UUID.class,
                BARBER_ID
        );
        customerId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO customers (id, name, phone, active, barbershop_id) VALUES (?, ?, ?, ?, ?)",
                customerId,
                "Concorrência Teste",
                "(11) 90000-" + customerId.toString().substring(0, 4),
                true,
                barbershopId
        );
    }

    @AfterEach
    void tearDown() {
        jdbc.update("DELETE FROM appointments WHERE customer_id = ?", customerId);
        jdbc.update("DELETE FROM customers WHERE id = ?", customerId);
    }

    @Test
    void postgresHasPartialUniqueIndexForScheduledSlots() {
        String index = jdbc.queryForObject(
                "SELECT to_regclass('public.ux_appointments_scheduled_slot')",
                String.class
        );

        assertEquals("ux_appointments_scheduled_slot", index);
    }

    @Test
    void concurrentSameSlotLeavesOneScheduledAppointment() throws Exception {
        LocalDateTime slot = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0).withSecond(0).withNano(0);
        List<InsertResult> results = runConcurrentInserts(slot, slot);

        assertEquals(1, results.stream().filter(InsertResult::success).count());
        assertEquals(1, results.stream().filter(result -> !result.success()).count());
        assertEquals(1, countScheduled(BARBER_ID, slot));
    }

    @Test
    void concurrentDifferentSlotsAreBothAllowed() throws Exception {
        LocalDateTime first = LocalDateTime.now().plusDays(6).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime second = first.plusMinutes(30);
        List<InsertResult> results = runConcurrentInserts(first, second);

        assertEquals(2, results.stream().filter(InsertResult::success).count());
        assertEquals(1, countScheduled(BARBER_ID, first));
        assertEquals(1, countScheduled(BARBER_ID, second));
    }

    @Test
    void canceledAppointmentDoesNotBlockReusingTheSlot() {
        LocalDateTime slot = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0);
        insert(BARBER_ID, customerId, slot, "CANCELED");

        assertDoesNotThrow(() -> insert(BARBER_ID, customerId, slot, "SCHEDULED"));
        assertEquals(1, countScheduled(BARBER_ID, slot));
    }

    @Test
    void movingAppointmentToOccupiedScheduledSlotIsRejectedByPostgres() {
        LocalDateTime source = LocalDateTime.now().plusDays(8).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime target = source.plusMinutes(30);
        UUID sourceAppointmentId = insert(BARBER_ID, customerId, source, "SCHEDULED");
        insert(BARBER_ID, customerId, target, "SCHEDULED");

        assertThrows(DataIntegrityViolationException.class, () ->
                new TransactionTemplate(transactionManager).execute(status -> {
                    jdbc.update(
                            "UPDATE appointments SET appointment_date_time = ? WHERE id = ?",
                            target,
                            sourceAppointmentId
                    );
                    return null;
                })
        );
        assertEquals(1, countScheduled(BARBER_ID, source));
        assertEquals(1, countScheduled(BARBER_ID, target));
    }

    @Test
    void scheduledSlotConflictIsTranslatedToControlledBusinessError() {
        LocalDateTime slot = LocalDateTime.now().plusDays(10)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        insert(BARBER_ID, customerId, slot, "SCHEDULED");
        AppointmentEntity duplicate = new AppointmentEntity(
                customerRepository.findById(customerId).orElseThrow(),
                barberRepository.findById(BARBER_ID).orElseThrow(),
                List.of(),
                BigDecimal.ZERO,
                slot,
                AppointmentStatus.SCHEDULED,
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> appointmentService.saveScheduledAppointment(duplicate)
        );

        assertEquals("Horário indisponível.", exception.getMessage());
    }

    @Test
    void concurrentAnticipationMoveAndNewReservationStillLeaveOneScheduled() throws Exception {
        LocalDateTime source = LocalDateTime.now().plusDays(9).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime target = source.minusDays(1);
        UUID sourceAppointmentId = insert(BARBER_ID, customerId, source, "SCHEDULED");
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<InsertResult> move = executor.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    new TransactionTemplate(transactionManager).execute(status -> {
                        jdbc.update(
                                "UPDATE appointments SET appointment_date_time = ? WHERE id = ?",
                                target,
                                sourceAppointmentId
                        );
                        return null;
                    });
                    return new InsertResult(true, null);
                } catch (Exception exception) {
                    return new InsertResult(false, exception);
                }
            });
            Future<InsertResult> reservation = executor.submit(() -> insertAfter(start, ready, target));
            ready.await();
            start.countDown();

            assertEquals(1, List.of(move.get(), reservation.get()).stream()
                    .filter(InsertResult::success)
                    .count());
            assertEquals(1, countScheduled(BARBER_ID, target));
        } finally {
            executor.shutdownNow();
        }
    }

    private List<InsertResult> runConcurrentInserts(
            LocalDateTime first,
            LocalDateTime second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<InsertResult>> futures = List.of(
                    executor.submit(() -> insertAfter(start, ready, first)),
                    executor.submit(() -> insertAfter(start, ready, second))
            );
            ready.await();
            start.countDown();
            List<InsertResult> results = new ArrayList<>();
            for (Future<InsertResult> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private InsertResult insertAfter(
            CountDownLatch start,
            CountDownLatch ready,
            LocalDateTime slot) {
        ready.countDown();
        try {
            start.await();
            new TransactionTemplate(transactionManager).execute(status -> {
                insert(BARBER_ID, customerId, slot, "SCHEDULED");
                return null;
            });
            return new InsertResult(true, null);
        } catch (Exception exception) {
            return new InsertResult(false, exception);
        }
    }

    private UUID insert(UUID barberId, UUID customer, LocalDateTime slot, String status) {
        UUID appointmentId = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO appointments (id, appointment_date_time, created_at, status, barber_id, customer_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                appointmentId,
                slot,
                LocalDateTime.now(),
                status,
                barberId,
                customer
        );
        return appointmentId;
    }

    private int countScheduled(UUID barberId, LocalDateTime slot) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE barber_id = ? AND appointment_date_time = ? AND status = 'SCHEDULED'",
                Integer.class,
                barberId,
                slot
        );
        return count == null ? 0 : count;
    }

    private record InsertResult(boolean success, Exception exception) {
    }

    private static void assertDoesNotThrow(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
