package com.barbersaas.push.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionMigrationTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsTableAndBothIndexesIdempotently() {
        PushSubscriptionMigration migration = new PushSubscriptionMigration(jdbcTemplate);

        migration.run(new DefaultApplicationArguments());

        verify(jdbcTemplate).execute(contains("CREATE TABLE IF NOT EXISTS push_subscriptions"));
        verify(jdbcTemplate).execute(contains("CREATE UNIQUE INDEX IF NOT EXISTS ux_push_subscriptions_endpoint"));
        verify(jdbcTemplate).execute(contains("CREATE INDEX IF NOT EXISTS ix_push_subscriptions_customer_id"));
        verify(jdbcTemplate, times(3)).execute(org.mockito.ArgumentMatchers.anyString());
    }
}
