package com.securemarts.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Runs Flyway repair before migrate so that checksum mismatches (e.g. migration file
 * changed after it was applied) are fixed on startup. After the first successful start
 * you can remove this bean if you prefer; repair is harmless when there is nothing to fix.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                flyway.repair();
                flyway.migrate();
            }
        };
    }
}
