package com.barbersaas.push.service;

import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.entity.PushSubscriptionEntity;
import com.barbersaas.push.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionPersistenceTest {

    private static final UUID CUSTOMER_A = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID CUSTOMER_B = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Mock
    private PushSubscriptionRepository subscriptions;

    @Mock
    private CustomerRepository customers;

    private WebPushService service;
    private CustomerEntity customerA;
    private CustomerEntity customerB;

    @BeforeEach
    void setUp() {
        PushProperties properties = new PushProperties();
        properties.setEnabled(true);
        properties.setVapidPublicKey("public");
        properties.setVapidPrivateKey("private");
        properties.setVapidSubject("mailto:test@example.com");
        service = new WebPushService(properties, subscriptions, customers);
        customerA = customer(CUSTOMER_A);
        customerB = customer(CUSTOMER_B);
    }

    @Test
    void persistsFirstSubscriptionAndAllowsAnotherDeviceForSameCustomer() {
        when(customers.findById(CUSTOMER_A)).thenReturn(Optional.of(customerA));
        when(subscriptions.upsertForCustomer(any(), any(), any(), any(), any())).thenReturn(1);
        service.save(request("https://push.example.test/a", CUSTOMER_A, "key-a", "auth-a"));
        service.save(request("https://push.example.test/b", CUSTOMER_A, "key-b", "auth-b"));

        verify(subscriptions, org.mockito.Mockito.times(2))
                .upsertForCustomer(any(), eq(CUSTOMER_A), any(), any(), any());
    }

    @Test
    void keepsSubscriptionsOfDifferentCustomersSeparate() {
        when(customers.findById(CUSTOMER_A)).thenReturn(Optional.of(customerA));
        when(customers.findById(CUSTOMER_B)).thenReturn(Optional.of(customerB));
        when(subscriptions.upsertForCustomer(any(), any(), any(), any(), any())).thenReturn(1);
        service.save(request("https://push.example.test/a", CUSTOMER_A, "key-a", "auth-a"));
        service.save(request("https://push.example.test/b", CUSTOMER_B, "key-b", "auth-b"));

        verify(subscriptions, org.mockito.Mockito.times(2))
                .upsertForCustomer(any(), any(), any(), any(), any());
        verify(customers).findById(CUSTOMER_A);
        verify(customers).findById(CUSTOMER_B);
    }

    @Test
    void sameEndpointForSameCustomerUpdatesKeysWithoutCreatingAnotherRow() {
        when(customers.findById(CUSTOMER_A)).thenReturn(Optional.of(customerA));
        when(subscriptions.upsertForCustomer(any(), any(), any(), any(), any())).thenReturn(1);
        service.save(request("https://push.example.test/a", CUSTOMER_A, "new", "new-auth"));

        verify(subscriptions).upsertForCustomer(
                any(), eq(CUSTOMER_A), eq("https://push.example.test/a"), eq("new"), eq("new-auth"));
    }

    @Test
    void rejectsEndpointAlreadyOwnedByAnotherCustomer() {
        when(customers.findById(CUSTOMER_B)).thenReturn(Optional.of(customerB));
        when(subscriptions.upsertForCustomer(any(), eq(CUSTOMER_B), any(), any(), any())).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(request("https://push.example.test/a", CUSTOMER_B, "new", "new-auth")));

        assertEquals("Subscription Web Push já está associada a outro cliente.", exception.getMessage());
    }

    @Test
    void rejectsUnknownCustomerBeforePersisting() {
        UUID unknown = UUID.fromString("30000000-0000-0000-0000-000000000001");
        when(customers.findById(unknown)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> service.save(request("https://push.example.test/a", unknown, "key", "auth")));

        verify(subscriptions, never()).upsertForCustomer(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsSubscriptionWithoutCustomer() {
        assertThrows(BusinessException.class,
                () -> service.save(new PushSubscriptionRequest(
                        "https://push.example.test/a", "key", "auth", null)));

        verifyNoPersistenceCall();
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 410})
    void removesOnlyTheSubscriptionRejectedAsGoneByTheProvider(int status) {
        PushProperties properties = enabledProperties();
        properties.setEnabled(true);
        PushSubscriptionEntity first = subscription(CUSTOMER_A, "https://push.example.test/a", "key-a", "auth-a");
        PushSubscriptionEntity second = subscription(CUSTOMER_A, "https://push.example.test/b", "key-b", "auth-b");
        when(subscriptions.findAllByCustomerId(CUSTOMER_A)).thenReturn(List.of(first, second));
        WebPushService deliveryService = new WebPushService(
                properties, subscriptions, customers,
                (target, payload) -> target.endpoint().endsWith("/a")
                        ? new WebPushService.ProviderResponse(status)
                        : new WebPushService.ProviderResponse(201));

        deliveryService.sendAvailabilityNotification(CUSTOMER_A,
                UUID.fromString("40000000-0000-0000-0000-000000000001"),
                UUID.fromString("50000000-0000-0000-0000-000000000001"));

        verify(subscriptions).deleteById(first.getId());
        verify(subscriptions, never()).deleteById(second.getId());
    }

    @Test
    void sendsEveryInterestToEveryDeviceWithoutDeduplicatingTheCustomer() {
        PushProperties properties = enabledProperties();
        PushSubscriptionEntity first = subscription(CUSTOMER_A, "https://push.example.test/a", "key-a", "auth-a");
        PushSubscriptionEntity second = subscription(CUSTOMER_A, "https://push.example.test/b", "key-b", "auth-b");
        when(subscriptions.findAllByCustomerId(CUSTOMER_A)).thenReturn(List.of(first, second));
        List<String> payloads = new ArrayList<>();
        WebPushService deliveryService = new WebPushService(
                properties, subscriptions, customers,
                (target, payload) -> {
                    payloads.add(target.endpoint() + ":" + payload);
                    return new WebPushService.ProviderResponse(201);
                });
        UUID interestA = UUID.fromString("40000000-0000-0000-0000-000000000001");
        UUID interestB = UUID.fromString("40000000-0000-0000-0000-000000000002");
        UUID slot = UUID.fromString("50000000-0000-0000-0000-000000000001");

        deliveryService.sendAvailabilityNotification(CUSTOMER_A, interestA, slot);
        deliveryService.sendAvailabilityNotification(CUSTOMER_A, interestB, slot);

        assertEquals(4, payloads.size());
        assertEquals(2, payloads.stream().filter(payload -> payload.contains(interestA.toString())).count());
        assertEquals(2, payloads.stream().filter(payload -> payload.contains(interestB.toString())).count());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 429, 500, 503})
    void providerFailureDoesNotDeleteOrStopTheOtherDevice(int status) {
        PushProperties properties = enabledProperties();
        PushSubscriptionEntity first = subscription(CUSTOMER_A, "https://push.example.test/a", "key-a", "auth-a");
        PushSubscriptionEntity second = subscription(CUSTOMER_A, "https://push.example.test/b", "key-b", "auth-b");
        when(subscriptions.findAllByCustomerId(CUSTOMER_A)).thenReturn(List.of(first, second));
        List<String> sent = new ArrayList<>();
        WebPushService deliveryService = new WebPushService(
                properties, subscriptions, customers,
                (target, payload) -> {
                    sent.add(target.endpoint());
                    if (target.endpoint().endsWith("/a")) {
                        throw new IOException("timeout");
                    }
                    return new WebPushService.ProviderResponse(status);
                });

        deliveryService.sendAvailabilityNotification(CUSTOMER_A,
                UUID.fromString("40000000-0000-0000-0000-000000000001"),
                UUID.fromString("50000000-0000-0000-0000-000000000001"));

        assertEquals(List.of("https://push.example.test/a", "https://push.example.test/b"), sent);
        verify(subscriptions, never()).deleteById(any());
    }

    private PushSubscriptionRequest request(String endpoint, UUID customerId, String p256dh, String auth) {
        return new PushSubscriptionRequest(endpoint, p256dh, auth, customerId);
    }

    private PushProperties enabledProperties() {
        PushProperties properties = new PushProperties();
        properties.setEnabled(true);
        properties.setVapidPublicKey("public");
        properties.setVapidPrivateKey("private");
        properties.setVapidSubject("mailto:test@example.com");
        return properties;
    }

    private PushSubscriptionEntity subscription(UUID customerId, String endpoint, String p256dh, String auth) {
        PushSubscriptionEntity entity = new PushSubscriptionEntity(customerAOrB(customerId), endpoint, p256dh, auth);
        entity.setId(UUID.nameUUIDFromBytes(endpoint.getBytes()));
        return entity;
    }

    private CustomerEntity customerAOrB(UUID id) {
        return id.equals(CUSTOMER_A) ? customerA : customerB;
    }

    private CustomerEntity customer(UUID id) {
        CustomerEntity customer = new CustomerEntity();
        org.springframework.test.util.ReflectionTestUtils.setField(customer, "id", id);
        return customer;
    }

    private void verifyNoPersistenceCall() {
        verify(subscriptions, never()).upsertForCustomer(any(), any(), any(), any(), any());
    }
}
