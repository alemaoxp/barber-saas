package com.barbersaas.push.service;

import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PushTestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushTestService.class);

    private static final String TEST_PAYLOAD = """
            {"title":"Barber SaaS","body":"Notificação de teste recebida com sucesso."}
            """;

    private final PushTestProperties properties;
    // ponytail: one local browser only; replace with authenticated persistence for production.
    private final AtomicReference<PushSubscriptionRequest> subscription =
            new AtomicReference<>();
    private final Map<UUID, PushSubscriptionRequest> customerSubscriptions =
            new ConcurrentHashMap<>();

    public PushTestService(PushTestProperties properties) {
        this.properties = properties;
    }

    public String publicKey() {
        requireEnabled();
        if (properties.getVapidPublicKey().isBlank()) {
            throw new BusinessException("Chave pública VAPID de teste não configurada.");
        }
        return properties.getVapidPublicKey();
    }

    public void save(PushSubscriptionRequest request) {
        requireEnabled();
        if (request.endpoint().isBlank() || request.p256dh().isBlank()
                || request.auth().isBlank()) {
            throw new BusinessException("Subscription de teste inválida.");
        }
        subscription.set(request);
        if (request.customerId() != null) {
            customerSubscriptions.put(request.customerId(), request);
            LOGGER.info("Subscription local associada ao cliente {}.", request.customerId());
        }
    }

    public void sendTestNotification() {
        PushSubscriptionRequest target = subscription.get();
        if (target == null) {
            throw new BusinessException("Ative a notificação de teste neste navegador primeiro.");
        }
        send(target, TEST_PAYLOAD, "teste");
    }

    public void sendAvailabilityNotification(UUID customerId) {
        PushSubscriptionRequest target = subscriptionForCustomer(customerId);
        if (target == null) {
            LOGGER.info("Cliente {} elegível sem subscription local ativa.", customerId);
            return;
        }
        send(target, """
                {"title":"Jhow Cortes","body":"Surgiu um horário mais cedo. Toque para conferir."}
                """, "oportunidade");
    }

    private void send(PushSubscriptionRequest target, String payload, String kind) {
        requireEnabled();
        if (properties.getVapidPrivateKey().isBlank()) {
            throw new BusinessException("Chave privada VAPID de teste não configurada.");
        }
        if (properties.getVapidPublicKey().isBlank()) {
            throw new BusinessException("Chave pública VAPID de teste não configurada.");
        }
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            PushService pushService = new PushService(
                    properties.getVapidPublicKey(),
                    properties.getVapidPrivateKey(),
                    properties.getVapidSubject());
            HttpResponse providerResponse = pushService.send(new Notification(
                    target.endpoint(), target.p256dh(), target.auth(),
                    payload.getBytes(StandardCharsets.UTF_8)), payloadEncoding());
            int providerStatus = providerResponse.getStatusLine().getStatusCode();
            if (!isSuccessfulProviderStatus(providerStatus)) {
                String providerBody = providerResponse.getEntity() == null ? ""
                        : EntityUtils.toString(providerResponse.getEntity(), StandardCharsets.UTF_8).strip();
                LOGGER.warn("Push provider rejeitou {} com HTTP {} e body: {}.",
                        kind, providerStatus, providerBody);
                throw new BusinessException(
                        "Push provider respondeu HTTP " + providerStatus
                                + (providerBody.isEmpty() ? "." : ": " + providerBody));
            }
            LOGGER.info("Push de {} aceito pelo provider com HTTP {}.", kind, providerStatus);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            String diagnostic = diagnosticMessage(exception);
            LOGGER.error("Falha no envio Web Push de {}: {}", kind, diagnostic, exception);
            throw new BusinessException("Web Push de " + kind + " falhou: " + diagnostic, exception);
        }
    }

    static Encoding payloadEncoding() {
        return Encoding.AES128GCM;
    }

    static boolean isSuccessfulProviderStatus(int status) {
        return status >= 200 && status < 300;
    }

    static String diagnosticMessage(Exception exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return exception.getClass().getSimpleName() + ": " + exception.getMessage()
                + "; root cause " + rootCause.getClass().getSimpleName()
                + ": " + rootCause.getMessage();
    }

    PushSubscriptionRequest subscription() {
        PushSubscriptionRequest target = subscription.get();
        if (target == null) {
            throw new BusinessException("Ative a notificação de teste neste navegador primeiro.");
        }
        return target;
    }

    PushSubscriptionRequest subscriptionForCustomer(UUID customerId) {
        return customerSubscriptions.get(customerId);
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException("Push de teste está desativado.");
        }
    }
}
