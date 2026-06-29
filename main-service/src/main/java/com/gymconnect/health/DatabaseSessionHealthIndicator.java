package com.gymconnect.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Custom health indicator that verifies database connectivity by opening
 * a test connection and checking its validity.
 */
@Component("databaseSession")
public class DatabaseSessionHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSessionHealthIndicator.class);
    private static final int CONNECTION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public DatabaseSessionHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        logger.debug("Checking database session health");
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(CONNECTION_TIMEOUT_SECONDS)) {
                logger.warn("Database connection is not valid");
                return Health.down()
                        .withDetail("reason", "Connection is not valid")
                        .build();
            }
            DatabaseMetaData meta = connection.getMetaData();
            logger.debug("Database health check passed: {}", meta.getDatabaseProductName());
            return Health.up()
                    .withDetail("database", meta.getDatabaseProductName())
                    .withDetail("version", meta.getDatabaseProductVersion())
                    .withDetail("url", meta.getURL())
                    .build();
        } catch (SQLException e) {
            logger.error("Database health check failed", e);
            return Health.down(e).build();
        }
    }
}
