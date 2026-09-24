package com.barbersaas.push.controller;

import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.service.WebPushService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicPushControllerTest {

    @Test
    void exposesPublicKeyAndSubscriptionEndpointsThroughNeutralService() {
        WebPushService service = Mockito.mock(WebPushService.class);
        when(service.publicKey()).thenReturn("public-key");
        PublicPushController controller = new PublicPushController(service);

        assertEquals("public-key", controller.publicKey().publicKey());
        PushSubscriptionRequest request = new PushSubscriptionRequest(
                "https://push.example.test/endpoint", "p256dh", "auth", UUID.randomUUID());
        assertEquals(204, controller.save(request).getStatusCode().value());
        verify(service).save(request);
    }

    @Test
    void publicRouteIsSeparateAndDevelopmentRouteIsLocalOnly() {
        assertEquals("/api/public/push",
                PublicPushController.class.getAnnotation(RequestMapping.class).value()[0]);
        assertTrue(PushTestController.class.isAnnotationPresent(Profile.class));
        assertEquals("local",
                PushTestController.class.getAnnotation(Profile.class).value()[0]);
    }
}
