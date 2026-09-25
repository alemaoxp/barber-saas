package com.barbersaas.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("bootstrap")
@ConditionalOnProperty(prefix = "bootstrap", name = "run", havingValue = "true", matchIfMissing = true)
public class BootstrapRunner implements ApplicationRunner {
    private final BootstrapProperties properties;
    private final BootstrapProvisioningService provisioningService;
    private final ConfigurableApplicationContext context;

    public BootstrapRunner(
            BootstrapProperties properties,
            BootstrapProvisioningService provisioningService,
            ConfigurableApplicationContext context) {
        this.properties = properties;
        this.provisioningService = provisioningService;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        properties.validate();
        provisioningService.provision();
        SpringApplication.exit(context, () -> 0);
    }
}
