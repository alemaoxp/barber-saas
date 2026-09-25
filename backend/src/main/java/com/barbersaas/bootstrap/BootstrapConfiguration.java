package com.barbersaas.bootstrap;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("bootstrap")
@EnableConfigurationProperties(BootstrapProperties.class)
public class BootstrapConfiguration {
}
