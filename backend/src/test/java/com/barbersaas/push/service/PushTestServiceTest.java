package com.barbersaas.push.service;

import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import nl.martijndwars.webpush.Encoding;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PushTestServiceTest {

    @Test
    void storesTheLatestLocalTestSubscription() {
        PushTestProperties properties = new PushTestProperties();
        properties.setEnabled(true);
        PushTestService service = new PushTestService(properties);

        service.save(new PushSubscriptionRequest(
                "https://push.example.test/first", "key-1", "auth-1", null));
        service.save(new PushSubscriptionRequest(
                "https://push.example.test/latest", "key-2", "auth-2", null));

        assertEquals("https://push.example.test/latest",
                service.subscription().endpoint());
    }

    @Test
    void retrievesTheSubscriptionAssociatedWithTheEligibleCustomer() {
        PushTestProperties properties = new PushTestProperties();
        properties.setEnabled(true);
        PushTestService service = new PushTestService(properties);
        UUID customerId = UUID.fromString("4817ecd7-1341-4830-a7e5-b351d4da3fe0");

        service.save(new PushSubscriptionRequest(
                "https://push.example.test/customer", "key", "auth", customerId));

        assertEquals("https://push.example.test/customer",
                service.subscriptionForCustomer(customerId).endpoint());
    }

    @Test
    void rejectsSubscriptionsWithoutBrowserKeys() {
        PushTestProperties properties = new PushTestProperties();
        properties.setEnabled(true);
        PushTestService service = new PushTestService(properties);

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
}
