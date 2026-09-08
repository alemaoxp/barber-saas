package com.barbersaas.push.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "push.test")
public class PushTestProperties {

    private boolean enabled;
    private String vapidPublicKey = "";
    private String vapidPrivateKey = "";
    private String vapidSubject = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    public void setVapidPublicKey(String vapidPublicKey) {
        this.vapidPublicKey = vapidPublicKey;
    }

    public String getVapidPrivateKey() {
        return vapidPrivateKey;
    }

    public void setVapidPrivateKey(String vapidPrivateKey) {
        this.vapidPrivateKey = vapidPrivateKey;
    }

    public String getVapidSubject() {
        return vapidSubject;
    }

    public void setVapidSubject(String vapidSubject) {
        this.vapidSubject = vapidSubject;
    }
}
