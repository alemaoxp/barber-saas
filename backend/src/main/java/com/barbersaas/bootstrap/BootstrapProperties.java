package com.barbersaas.bootstrap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "bootstrap")
public class BootstrapProperties {
    public static final UUID DEFAULT_BARBERSHOP_ID =
            UUID.fromString("9a5b3e91-cb71-4d77-a9d2-e1b35f4e2101");
    public static final UUID DEFAULT_BARBER_ID =
            UUID.fromString("3700633c-35f1-4ab9-af18-c60f8eb23b45");

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private boolean run = true;
    private UUID barbershopId = DEFAULT_BARBERSHOP_ID;
    private String barbershopName = "Jhow Cortes";
    private UUID barberId = DEFAULT_BARBER_ID;
    private String barberName = "Jhow";
    private String barberEmail = "jhow@jhowcortes.dev";
    private String barberPhone = "(11) 99999-9999";
    private String adminName;
    private String adminEmail;
    private String adminPassword;

    public void validate() {
        if (barbershopId == null || barberId == null) {
            throw new BootstrapException("Bootstrap exige os identificadores da Barbershop e do Barber.");
        }
        requireText(adminName, "BOOTSTRAP_ADMIN_NAME");
        String normalizedEmail = adminEmail == null ? "" : adminEmail.trim();
        if (!EMAIL.matcher(normalizedEmail).matches()) {
            throw new BootstrapException("BOOTSTRAP_ADMIN_EMAIL ausente ou inválido.");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new BootstrapException("BOOTSTRAP_ADMIN_PASSWORD é obrigatório.");
        }
    }

    private void requireText(String value, String variable) {
        if (value == null || value.isBlank()) {
            throw new BootstrapException(variable + " é obrigatório.");
        }
    }

    public boolean isRun() { return run; }
    public void setRun(boolean run) { this.run = run; }
    public UUID getBarbershopId() { return barbershopId; }
    public void setBarbershopId(UUID barbershopId) { this.barbershopId = barbershopId; }
    public String getBarbershopName() { return barbershopName; }
    public void setBarbershopName(String barbershopName) { this.barbershopName = barbershopName; }
    public UUID getBarberId() { return barberId; }
    public void setBarberId(UUID barberId) { this.barberId = barberId; }
    public String getBarberName() { return barberName; }
    public void setBarberName(String barberName) { this.barberName = barberName; }
    public String getBarberEmail() { return barberEmail; }
    public void setBarberEmail(String barberEmail) { this.barberEmail = barberEmail; }
    public String getBarberPhone() { return barberPhone; }
    public void setBarberPhone(String barberPhone) { this.barberPhone = barberPhone; }
    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public String getAdminEmail() { return adminEmail == null ? null : adminEmail.trim(); }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}
