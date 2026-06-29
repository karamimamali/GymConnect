package com.gymconnect.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseSessionHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    private DatabaseSessionHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new DatabaseSessionHealthIndicator(dataSource);
    }

    @Test
    void health_shouldReturnUp_whenConnectionIsValid() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("2.2.224");
        when(databaseMetaData.getURL()).thenReturn("jdbc:h2:mem:gymconnect");

        Health health = indicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("H2", health.getDetails().get("database"));
        assertEquals("2.2.224", health.getDetails().get("version"));
        assertNotNull(health.getDetails().get("url"));
    }

    @Test
    void health_shouldReturnDown_whenConnectionIsNotValid() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(false);

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Connection is not valid", health.getDetails().get("reason"));
    }

    @Test
    void health_shouldReturnDown_whenSQLExceptionThrown() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        Health health = indicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
