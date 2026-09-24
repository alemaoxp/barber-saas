package com.barbersaas.push.service;

import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.entity.PushSubscriptionEntity;
import com.barbersaas.push.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PushTestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushTestService.class);

    private static final String TEST_PAYLOAD = """
            {"title":"Barber SaaS","body":"Notificação de teste recebida com sucesso."}
            """;

    private final PushTestProperties properties;
    private final PushSubscriptionRepository subscriptions;
    private final CustomerRepository customers;
    // Local-only diagnostic endpoint state. Real availability delivery reads the repository below.
    private final AtomicReference<PushSubscriptionRequest> subscription =
            new AtomicReference<>();
    private final PushDelivery delivery;

    @Autowired
    public PushTestService(
            PushTestProperties properties,
            PushSubscriptionRepository subscriptions,
            CustomerRepository customers) {
        this.properties = properties;
        this.subscriptions = subscriptions;
        this.customers = customers;
        this.delivery = this::deliver;
    }

    PushTestService(
            PushTestProperties properties,
            PushSubscriptionRepository subscriptions,
            CustomerRepository customers,
            PushDelivery delivery) {
        this.properties = properties;
        this.subscriptions = subscriptions;
        this.customers = customers;
        this.delivery = delivery;
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
        if (request.customerId() == null) {
            throw new BusinessException("Cliente da subscription é obrigatório.");
        }

        if (subscriptions == null || customers == null) {
            throw new BusinessException("Persistência de subscriptions não está configurada.");
        }

        customers.findById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cliente da subscription não encontrado."));

        int affectedRows = subscriptions.upsertForCustomer(
                UUID.randomUUID(),
                request.customerId(),
                request.endpoint(),
                request.p256dh(),
                request.auth());
        if (affectedRows == 0) {
            throw new BusinessException(
                    "Subscription Web Push já está associada a outro cliente.");
        }
        subscription.set(request);
        LOGGER.info("Subscription Web Push persistida para o cliente {}.", request.customerId());
    }

    public void sendTestNotification() {
        PushSubscriptionRequest target = subscription.get();
        if (target == null) {
            throw new BusinessException("Ative a notificação de teste neste navegador primeiro.");
        }
        send(target, TEST_PAYLOAD, "teste");
    }

    public void sendAvailabilityNotification(
            UUID customerId,
            UUID availabilityInterestId,
            UUID availableSlotId) {
        if (subscriptions == null) {
            LOGGER.info("Cliente {} elegível sem repository de subscriptions configurado.", customerId);
            return;
        }
        List<PushSubscriptionEntity> targets = subscriptions.findAllByCustomerId(customerId);
        if (targets.isEmpty()) {
            LOGGER.info("Cliente {} elegível sem subscription persistida ativa.", customerId);
            return;
        }

        String payload = availabilityPayload(availabilityInterestId, availableSlotId);
        for (PushSubscriptionEntity entity : targets) {
            try {
                ProviderResponse response = delivery.send(toRequest(entity), payload);
                int status = response.status();
                if (status == 404 || status == 410) {
                    subscriptions.deleteById(entity.getId());
                    LOGGER.info("Subscription {} removida após resposta HTTP {} do provider.",
                            entity.getId(), status);
                } else if (isSuccessfulProviderStatus(status)) {
                    LOGGER.info("Push de oportunidade aceito para subscription {} com HTTP {}.",
                            entity.getId(), status);
                } else {
                    LOGGER.warn("Push de oportunidade rejeitado para subscription {} com HTTP {}.",
                            entity.getId(), status);
                }
            } catch (Exception exception) {
                LOGGER.warn("Falha no Push de oportunidade para subscription {} do cliente {}: {}.",
                        entity.getId(), customerId, safeDiagnosticMessage(exception));
            }
        }
    }

    static String availabilityPayload(UUID availabilityInterestId, UUID availableSlotId) {
        return """
                {"title":"Jhow Cortes","body":"Surgiu um horário mais cedo. Toque para conferir.","availableSlotId":"%s","availabilityInterestId":"%s"}
                """.formatted(availableSlotId, availabilityInterestId);
    }

    private void send(PushSubscriptionRequest target, String payload, String kind) {
        requireEnabled();
        try {
            ProviderResponse response = delivery.send(target, payload);
            if (!isSuccessfulProviderStatus(response.status())) {
                throw new BusinessException(
                        "Push provider respondeu HTTP " + response.status() + ".");
            }
            LOGGER.info("Push de {} aceito pelo provider com HTTP {}.", kind, response.status());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            String diagnostic = safeDiagnosticMessage(exception);
            LOGGER.error("Falha no envio Web Push de {}: {}", kind, diagnostic);
            throw new BusinessException("Web Push de " + kind + " falhou: " + diagnostic, exception);
        }
    }

    private ProviderResponse deliver(PushSubscriptionRequest target, String payload)
            throws Exception {
        requireEnabled();
        if (properties.getVapidPrivateKey().isBlank()) {
            throw new BusinessException("Chave privada VAPID de teste não configurada.");
        }
        if (properties.getVapidPublicKey().isBlank()) {
            throw new BusinessException("Chave pública VAPID de teste não configurada.");
        }
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
        if (providerResponse.getEntity() != null) {
            EntityUtils.consumeQuietly(providerResponse.getEntity());
        }
        return new ProviderResponse(providerStatus);
    }

    private PushSubscriptionRequest toRequest(PushSubscriptionEntity entity) {
        return new PushSubscriptionRequest(
                entity.getEndpoint(), entity.getP256dh(), entity.getAuth(),
                entity.getCustomer().getId());
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

    static String safeDiagnosticMessage(Exception exception) {
        return diagnosticMessage(exception)
                .replaceAll("https?://\\S+", "[redacted-url]")
                .replaceAll("(?i)(endpoint|auth|p256dh)(\\s*[=:]\\s*)\\S+", "$1$2[redacted]");
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

    @FunctionalInterface
    interface PushDelivery {
        ProviderResponse send(PushSubscriptionRequest target, String payload) throws Exception;
    }

    record ProviderResponse(int status) {
    }
}
