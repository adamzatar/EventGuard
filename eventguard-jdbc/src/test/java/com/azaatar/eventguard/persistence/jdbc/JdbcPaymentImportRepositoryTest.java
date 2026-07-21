package com.azaatar.eventguard.persistence.jdbc;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.persistence.PaymentImportAttempt;
import com.azaatar.eventguard.persistence.PersistenceException;
import com.azaatar.eventguard.pojo.ParseStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdbcPaymentImportRepositoryTest {

    @Test
    public void givenNullConnectionProviderWhenCreatedThenThrowsNullPointerException() {
        // Arrange
        ConnectionProvider connectionProvider = null;

        // Act and Assert
        assertThrows(
                NullPointerException.class,
                () -> new JdbcPaymentImportRepository(connectionProvider)
        );
    }

    @Test
    public void givenNullImportAttemptWhenSaveThenThrowsNullPointerException() {
        // Arrange
        ConnectionProvider connectionProvider = new FailingConnectionProvider();
        JdbcPaymentImportRepository repository = new JdbcPaymentImportRepository(connectionProvider);

        // Act and Assert
        assertThrows(
                NullPointerException.class,
                () -> repository.save(null)
        );
    }

    @Test
    public void givenConnectionProviderThrowsSQLExceptionWhenSaveThenThrowsPersistenceException() {
        // Arrange
        ConnectionProvider connectionProvider = new FailingConnectionProvider();
        JdbcPaymentImportRepository repository = new JdbcPaymentImportRepository(connectionProvider);
        PaymentImportAttempt validAttempt = createSuccessfulImportAttempt();

        // Act and Assert
        assertThrows(
                PersistenceException.class,
                () -> repository.save(validAttempt)
        );
    }

    @Test
    public void givenConnectionProviderThrowsSQLExceptionWhenSaveThenPersistenceExceptionKeepsCause() {
        // Arrange
        SQLException expectedCause = new SQLException("Database unavailable");
        ConnectionProvider connectionProvider = new FailingConnectionProvider(expectedCause);
        JdbcPaymentImportRepository repository = new JdbcPaymentImportRepository(connectionProvider);

        // Act
        PersistenceException actualException = assertThrows(
                PersistenceException.class,
                () -> repository.save(createSuccessfulImportAttempt())
        );

        // Assert
        assertSame(expectedCause, actualException.getCause());
    }

    private PaymentImportAttempt createSuccessfulImportAttempt() {
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        return new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records);
    }

    private PaymentRecord createPaymentRecord() {
        return new PaymentRecord(
                "PAY-001",
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JOD",
                PaymentStatus.PENDING
        );
    }

    private static class FailingConnectionProvider implements ConnectionProvider {

        private final SQLException exception;

        private FailingConnectionProvider() {
            this(new SQLException("Database unavailable"));
        }

        private FailingConnectionProvider(SQLException exception) {
            this.exception = exception;
        }

        @Override
        public Connection getConnection() throws SQLException {
            throw exception;
        }
    }
}