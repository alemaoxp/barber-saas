package com.barbersaas.push.service;

import com.barbersaas.customers.repository.CustomerRepository;
import com.barbersaas.exception.BusinessException;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.entity.PushSubscriptionEntity;
import com.barbersaas.push.repository.PushSubscriptionRepository;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;
import java.util.UUID;

@Service
public class WebPushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebPushService.class);
    private final PushProperties properties;
    private final PushSubscriptionRepository subscriptions;
    private final CustomerRepository customers;
    private final PushDelivery delivery;

    @Autowired
    public WebPushService(
            PushProperties properties,
            PushSubscriptionRepository subscriptions,
            CustomerRepository customers) {
        this(properties, subscriptions, customers, null);
    }

    WebPushService(
            PushProperties properties,
            PushSubscriptionRepository subscriptions,
            CustomerRepository customers,
            PushDelivery delivery) {
        this.properties = properties;
        this.subscriptions = subscriptions;
        this.customers = customers;
        this.delivery = delivery == null ? this::deliver : delivery;
        validateConfiguration();
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public String publicKey() {
        requireEnabled();
        if (properties.getVapidPublicKey().isBlank()) {
            throw new BusinessException("Chave pública VAPID não configurada.");
        }
        return properties.getVapidPublicKey();
    }

    public void save(PushSubscriptionRequest request) {
        requireEnabled();
        if (request.endpoint().isBlank() || request.p256dh().isBlank()
                || request.auth().isBlank()) {
            throw new BusinessException("Subscription Web Push inválida.");
        }
        if (request.customerId() == null) {
            throw new BusinessException("Cliente da subscription é obrigatório.");
        }
        customers.findById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cliente da subscription não encontrado."));

        int affectedRows = subscriptions.upsertForCustomer(
                UUID.randomUUID(), request.customerId(), request.endpoint(),
                request.p256dh(), request.auth());
        if (affectedRows == 0) {
            throw new BusinessException("Subscription Web Push já está associada a outro cliente.");
        }
        LOGGER.info("Subscription Web Push persistida para o cliente {}.", request.customerId());
    }

    public void sendAvailabilityNotification(
            UUID customerId,
            UUID availabilityInterestId,
            UUID availableSlotId) {
        if (!properties.isEnabled()) {
            LOGGER.warn("Web Push desabilitado; oportunidade {} não será enviada.", availableSlotId);
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

    public void sendDiagnosticNotification(PushSubscriptionRequest target) {
        requireEnabled();
        try {
            ProviderResponse response = delivery.send(target,
                    "{\"title\":\"Barber SaaS\",\"body\":\"Notificação de teste recebida com sucesso.\"}");
            if (!isSuccessfulProviderStatus(response.status())) {
                throw new BusinessException("Push provider respondeu HTTP " + response.status() + ".");
            }
            LOGGER.info("Push de teste aceito pelo provider com HTTP {}.", response.status());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            String diagnostic = safeDiagnosticMessage(exception);
            LOGGER.error("Falha no envio Web Push de teste: {}", diagnostic);
            throw new BusinessException("Web Push de teste falhou: " + diagnostic, exception);
        }
    }

    static String availabilityPayload(UUID availabilityInterestId, UUID availableSlotId) {
        return """
                {"title":"Jhow Cortes","body":"Surgiu um horário mais cedo. Toque para conferir.","availableSlotId":"%s","availabilityInterestId":"%s"}
                """.formatted(availableSlotId, availabilityInterestId);
    }

    private ProviderResponse deliver(PushSubscriptionRequest target, String payload)
            throws Exception {
        requireEnabled();
        if (properties.getVapidPrivateKey().isBlank() || properties.getVapidPublicKey().isBlank()) {
            throw new BusinessException("Configuração VAPID não configurada.");
        }
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        nl.martijndwars.webpush.PushService pushService = new nl.martijndwars.webpush.PushService(
                properties.getVapidPublicKey(), properties.getVapidPrivateKey(),
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
        return new PushSubscriptionRequest(entity.getEndpoint(), entity.getP256dh(),
                entity.getAuth(), entity.getCustomer().getId());
    }

    private void requireEnabled() {
        if (!properties.isEnabled()) {
            throw new BusinessException("Web Push está desabilitado.");
        }
    }

    private void validateConfiguration() {
        if (properties.isEnabled() && (properties.getVapidPublicKey().isBlank()
                || properties.getVapidPrivateKey().isBlank()
                || properties.getVapidSubject().isBlank())) {
            throw new IllegalStateException("Web Push habilitado, mas a configuração VAPID está incompleta.");
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

    static String safeDiagnosticMessage(Exception exception) {
        return diagnosticMessage(exception)
                .replaceAll("https?://\\S+", "[redacted-url]")
                .replaceAll("(?i)(endpoint|auth|p256dh)(\\s*[=:]\\s*)\\S+", "$1$2[redacted]");
    }

    @FunctionalInterface
    interface PushDelivery {
        ProviderResponse send(PushSubscriptionRequest target, String payload) throws Exception;
    }

    record ProviderResponse(int status) {
    }
}
