package com.barbersaas.push.service;

import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

/** Local diagnostic adapter for the development-only push endpoint. */
@Service
@Profile("local")
public class PushTestService {

    private final PushTestProperties properties;
    private final WebPushService webPush;
    private final AtomicReference<PushSubscriptionRequest> subscription =
            new AtomicReference<>();

    public PushTestService(PushTestProperties properties, WebPushService webPush) {
        this.properties = properties;
        this.webPush = webPush;
    }

    public String publicKey() {
        requireEnabled();
        return webPush.publicKey();
    }

    public void save(PushSubscriptionRequest request) {
        requireEnabled();
        if (request.endpoint().isBlank() || request.p256dh().isBlank()
                || request.auth().isBlank()) {
            throw new BusinessException("Subscription de teste inválida.");
        }
        webPush.save(request);
        subscription.set(request);
    }

    public void sendTestNotification() {
        requireEnabled();
        PushSubscriptionRequest target = subscription();
        webPush.sendDiagnosticNotification(target);
    }

    PushSubscriptionRequest subscription() {
        PushSubscriptionRequest target = subscription.get();
        if (target == null) {
            throw new BusinessException("Ative a notificação de teste neste navegador primeiro.");
        }
        return target;
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException("Push de teste está desativado.");
        }
    }
}
