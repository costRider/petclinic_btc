package org.springframework.samples.petclinic.repository.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
 * Utility for validating JDBC connections and attempting a reconnection when needed.
 */
public final class JdbcConnectionValidator {

    private JdbcConnectionValidator() {
    }

    public static void ensureConnection(DataSource dataSource) {
        ensureConnection(dataSource, null);
    }

    public static void ensureConnection(DataSource dataSource, Runnable onReconnect) {
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null || !connection.isValid(2)) {
                attemptReconnect(dataSource, onReconnect, null);
            }
        } catch (SQLException ex) {
            attemptReconnect(dataSource, onReconnect, ex);
        }
    }

    private static void attemptReconnect(DataSource dataSource, Runnable onReconnect, SQLException cause) {
        if (onReconnect != null) {
            onReconnect.run();
        }
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null || !connection.isValid(2)) {
                throw new CannotGetJdbcConnectionException("Unable to validate JDBC connection after retry", cause);
            }
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Unable to obtain JDBC connection after retry", ex);
        }
    }
}
