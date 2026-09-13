package com.barbersaas.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.auth")
public class AdminAuthProperties {
    private int sessionDurationDays = 30;
    private String devEmail;
    private String devPassword;
    private String devName = "Jhow";

    public int getSessionDurationDays() { return sessionDurationDays; }
    public void setSessionDurationDays(int sessionDurationDays) { this.sessionDurationDays = sessionDurationDays; }
    public String getDevEmail() { return devEmail; }
    public void setDevEmail(String devEmail) { this.devEmail = devEmail; }
    public String getDevPassword() { return devPassword; }
    public void setDevPassword(String devPassword) { this.devPassword = devPassword; }
    public String getDevName() { return devName; }
    public void setDevName(String devName) { this.devName = devName; }
}
