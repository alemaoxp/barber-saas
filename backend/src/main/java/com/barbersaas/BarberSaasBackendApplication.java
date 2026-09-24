package com.barbersaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
public class BarberSaasBackendApplication {

    @Bean
    Clock applicationClock() {
        return Clock.systemDefaultZone();
    }

    public static void main(String[] args) {
        configureTimeZone(System.getenv().getOrDefault(
                "APP_TIMEZONE", "America/Sao_Paulo"));
        SpringApplication.run(BarberSaasBackendApplication.class, args);
    }

    static void configureTimeZone(String zoneId) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(zoneId)));
    }

}
