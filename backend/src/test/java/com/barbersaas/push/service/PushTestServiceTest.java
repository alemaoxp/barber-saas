package com.barbersaas.push.service;

import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

class PushTestServiceTest {

    @Test
    void storesTheLatestLocalTestSubscription() {
        PushTestProperties properties = enabledProperties();
        WebPushService webPush = Mockito.mock(WebPushService.class);
        PushTestService service = new PushTestService(properties, webPush);
        UUID customerId = UUID.fromString("4817ecd7-1341-4830-a7e5-b351d4da3fe0");

        PushSubscriptionRequest first = new PushSubscriptionRequest(
                "https://push.example.test/first", "key-1", "auth-1", customerId);
        PushSubscriptionRequest latest = new PushSubscriptionRequest(
                "https://push.example.test/latest", "key-2", "auth-2", customerId);
        service.save(first);
        service.save(latest);

        verify(webPush).save(first);
        verify(webPush).save(latest);
        assertEquals(latest, service.subscription());
    }

    @Test
    void rejectsSubscriptionsWithoutBrowserKeys() {
        PushTestService service = new PushTestService(
                enabledProperties(), Mockito.mock(WebPushService.class));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.save(new PushSubscriptionRequest(
                        "https://push.example.test/subscription", "", "", null)));

        assertEquals("Subscription de teste inválida.", exception.getMessage());
    }

    private PushTestProperties enabledProperties() {
        PushTestProperties properties = new PushTestProperties();
        properties.setEnabled(true);
        return properties;
    }
}
