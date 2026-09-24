package com.barbersaas.push.controller;

import com.barbersaas.push.dto.PushPublicKeyResponse;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.service.WebPushService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/push")
public class PublicPushController {

    private final WebPushService webPushService;

    public PublicPushController(WebPushService webPushService) {
        this.webPushService = webPushService;
    }

    @GetMapping("/public-key")
    public PushPublicKeyResponse publicKey() {
        return new PushPublicKeyResponse(webPushService.publicKey());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Void> save(@Valid @RequestBody PushSubscriptionRequest request) {
        webPushService.save(request);
        return ResponseEntity.noContent().build();
    }
}
