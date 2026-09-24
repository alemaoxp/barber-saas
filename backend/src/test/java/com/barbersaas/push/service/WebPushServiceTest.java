package com.barbersaas.push.service;

import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Encoding;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebPushServiceTest {

    @Test
    void rejectsIncompleteVapidConfigurationWhenEnabled() {
        PushProperties properties = new PushProperties();
        properties.setEnabled(true);
        properties.setVapidPublicKey("public-key");
        properties.setVapidPrivateKey("private-key");
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new WebPushService(properties, Mockito.mock(PushSubscriptionRepository.class),
                        Mockito.mock(CustomerRepository.class)));

        assertEquals("Web Push habilitado, mas a configuração VAPID está incompleta.",
                exception.getMessage());
        assertTrue(!exception.getMessage().contains("public-key"));
        assertTrue(!exception.getMessage().contains("private-key"));
    }

    @Test
    void disabledPushDoesNotReadSubscriptions() {
        PushProperties properties = new PushProperties();
        PushSubscriptionRepository subscriptions = Mockito.mock(PushSubscriptionRepository.class);
        WebPushService service = new WebPushService(properties, subscriptions,
                Mockito.mock(CustomerRepository.class));

        assertThrows(BusinessException.class, service::publicKey);
        service.sendAvailabilityNotification(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        Mockito.verifyNoInteractions(subscriptions);
    }

    @Test
    void keepsPayloadEncodingAndInterestIdentity() {
        UUID firstInterest = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondInterest = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID slot = UUID.fromString("00000000-0000-0000-0000-000000000003");

        String firstPayload = WebPushService.availabilityPayload(firstInterest, slot);
        String secondPayload = WebPushService.availabilityPayload(secondInterest, slot);

        assertEquals(Encoding.AES128GCM, WebPushService.payloadEncoding());
        assertTrue(firstPayload.contains("\"availableSlotId\":\"" + slot + "\""));
        assertTrue(firstPayload.contains("\"availabilityInterestId\":\"" + firstInterest + "\""));
        assertTrue(secondPayload.contains("\"availabilityInterestId\":\"" + secondInterest + "\""));
        assertNotEquals(firstPayload, secondPayload);
    }

    @Test
    void exposesProviderRootCauseWithoutChangingDiagnosticFormat() {
        IOException exception = new IOException("transport failed",
                new IllegalStateException("invalid VAPID key pair"));

        assertEquals(
                "IOException: transport failed; root cause IllegalStateException: invalid VAPID key pair",
                WebPushService.diagnosticMessage(exception));
    }
}
