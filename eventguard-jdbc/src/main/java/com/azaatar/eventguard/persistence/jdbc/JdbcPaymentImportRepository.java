package com.azaatar.eventguard.persistence.jdbc;

import com.azaatar.eventguard.persistence.PaymentImportAttempt;
import com.azaatar.eventguard.persistence.PaymentImportRepository;
import com.azaatar.eventguard.persistence.PersistenceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.Objects;

public class JdbcPaymentImportRepository implements PaymentImportRepository {

    private final ConnectionProvider connectionProvider;
    private static final String INSERT_IMPORT_ATTEMPT_SQL = """
            INSERT INTO payment_imports (
                source_name,
                imported_at,
                parse_status,
                description
            )
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

    public JdbcPaymentImportRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = Objects.requireNonNull(connectionProvider, "connectionProvider must not be null");
    }

    @Override
    public long save(PaymentImportAttempt importAttempt) {
        Objects.requireNonNull(importAttempt, "attempt must not be null");

        try (Connection connection = connectionProvider.getConnection()) {
            try {
                connection.setAutoCommit(false);
                long importId = insertImportAttempt(connection, importAttempt);
                connection.commit();
                return importId;
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw new PersistenceException("Failed to save payment import attempt", exception);
            }
        } catch (SQLException exception) {
            throw new PersistenceException("Failed to access database while saving payment import attempt", exception);
        }
    }

    private long insertImportAttempt(Connection connection, PaymentImportAttempt importAttempt) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_IMPORT_ATTEMPT_SQL)) {
            preparedStatement.setString(1, importAttempt.getSourceName());
            preparedStatement.setTimestamp(2, Timestamp.valueOf(importAttempt.getImportedAt()));
            preparedStatement.setString(3, importAttempt.getParseStatus().name());
            preparedStatement.setString(4, importAttempt.getDescription());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
            }
            throw new PersistenceException("No import ID returned after saving payment import attempt");
        }
    }

    private void rollback(Connection connection, SQLException originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}