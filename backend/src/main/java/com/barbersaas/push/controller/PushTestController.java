package com.barbersaas.push.controller;

import com.barbersaas.push.dto.PushPublicKeyResponse;
import com.barbersaas.push.dto.PushSubscriptionRequest;
import com.barbersaas.push.service.PushTestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** DEVELOPMENT ONLY: intentionally disabled unless PUSH_TEST_ENABLED=true. */
@RestController
@RequestMapping("/api/dev/push")
public class PushTestController {

    private final PushTestService pushTestService;

    public PushTestController(PushTestService pushTestService) {
        this.pushTestService = pushTestService;
    }

    @GetMapping("/public-key")
    public PushPublicKeyResponse publicKey() {
        return new PushPublicKeyResponse(pushTestService.publicKey());
    }

    @PostMapping("/subscription")
    public ResponseEntity<Void> save(@Valid @RequestBody PushSubscriptionRequest request) {
        pushTestService.save(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send() {
        pushTestService.sendTestNotification();
        return ResponseEntity.noContent().build();
    }
}
