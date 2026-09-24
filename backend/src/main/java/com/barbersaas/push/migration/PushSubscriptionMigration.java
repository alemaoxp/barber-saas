package com.barbersaas.push.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PushSubscriptionMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public PushSubscriptionMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS push_subscriptions (
                    id UUID PRIMARY KEY,
                    customer_id UUID NOT NULL,
                    endpoint VARCHAR(2048) NOT NULL,
                    p256dh TEXT NOT NULL,
                    auth TEXT NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL,
                    CONSTRAINT fk_push_subscriptions_customer
                        FOREIGN KEY (customer_id) REFERENCES customers(id)
                )
                """);
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_push_subscriptions_endpoint
                ON push_subscriptions (endpoint)
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS ix_push_subscriptions_customer_id
                ON push_subscriptions (customer_id)
                """);
    }
}
