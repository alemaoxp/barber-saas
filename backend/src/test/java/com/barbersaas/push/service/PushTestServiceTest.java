package com.barbersaas.push.service;

import com.barbersaas.exception.BusinessException;
import com.barbersaas.customers.entity.CustomerEntity;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Encoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushTestServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptions;

    @Mock
    private com.barbersaas.customers.repository.CustomerRepository customers;

    @Test
    void storesTheLatestLocalTestSubscription() {
        PushTestProperties properties = new PushTestProperties();
        properties.setEnabled(true);
        UUID customerId = UUID.fromString("4817ecd7-1341-4830-a7e5-b351d4da3fe0");
        CustomerEntity customer = new CustomerEntity();
        ReflectionTestUtils.setField(customer, "id", customerId);
        when(customers.findById(customerId)).thenReturn(java.util.Optional.of(customer));
        when(subscriptions.upsertForCustomer(any(), any(), any(), any(), any())).thenReturn(1);
        PushTestService service = new PushTestService(properties, subscriptions, customers);

        service.save(new PushSubscriptionRequest(
                "https://push.example.test/first", "key-1", "auth-1", customerId));
        service.save(new PushSubscriptionRequest(
                "https://push.example.test/latest", "key-2", "auth-2", customerId));

        assertEquals("https://push.example.test/latest",
                service.subscription().endpoint());
    }

    @Test
    void rejectsSubscriptionsWithoutBrowserKeys() {
        PushTestProperties properties = new PushTestProperties();
        properties.setEnabled(true);
        PushTestService service = new PushTestService(properties, subscriptions, customers);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new PushSubscriptionRequest(
                        "https://push.example.test/subscription", "", "", null)));

        assertEquals("Subscription de teste inválida.", exception.getMessage());
    }

    @Test
    void usesTheCurrentWebPushPayloadEncoding() {
        assertEquals(Encoding.AES128GCM, PushTestService.payloadEncoding());
    }

    @Test
    void recognizesProviderErrorsInsteadOfTreatingThemAsSent() {
        assertEquals(true, PushTestService.isSuccessfulProviderStatus(201));
        assertEquals(true, PushTestService.isSuccessfulProviderStatus(204));
        assertEquals(false, PushTestService.isSuccessfulProviderStatus(400));
    }

    @Test
    void exposesTheExceptionAndRootCauseInTheDevelopmentDiagnostic() {
        IOException exception = new IOException("transport failed",
                new IllegalStateException("invalid VAPID key pair"));

        assertEquals(
                "IOException: transport failed; root cause IllegalStateException: invalid VAPID key pair",
                PushTestService.diagnosticMessage(exception));
    }

    @Test
    void availabilityPayloadIdentifiesTheInterestAndSlot() {
        UUID firstInterest = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondInterest = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID slot = UUID.fromString("00000000-0000-0000-0000-000000000003");

        String firstPayload = PushTestService.availabilityPayload(firstInterest, slot);
        String secondPayload = PushTestService.availabilityPayload(secondInterest, slot);

        assertTrue(firstPayload.contains("\"availableSlotId\":\"" + slot + "\""));
        assertTrue(firstPayload.contains("\"availabilityInterestId\":\"" + firstInterest + "\""));
        assertTrue(secondPayload.contains("\"availabilityInterestId\":\"" + secondInterest + "\""));
        assertNotEquals(firstPayload, secondPayload);
    }
}
