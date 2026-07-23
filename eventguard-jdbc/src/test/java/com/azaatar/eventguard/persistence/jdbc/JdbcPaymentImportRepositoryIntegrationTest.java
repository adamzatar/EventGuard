package com.azaatar.eventguard.persistence.jdbc;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.domain.RejectionStatus;
import com.azaatar.eventguard.persistence.PaymentImportAttempt;
import com.azaatar.eventguard.persistence.PersistenceException;
import com.azaatar.eventguard.pojo.ParseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class JdbcPaymentImportRepositoryIntegrationTest {

    private static final String TEST_SOURCE_PREFIX = "integration-test-";
    private static final LocalDateTime IMPORTED_AT = LocalDateTime.of(2026, 7, 19, 10, 0);

    @BeforeEach
    public void cleanDatabase() throws SQLException {
        assumeTrue(databaseEnvironmentIsConfigured());
        cleanIntegrationTestRows();
    }

    @Test
    public void givenSuccessfulAttemptWhenSavedThenCreatesImportAndPaymentRows() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createSuccessfulAttempt(sourceName);

        // Act
        long importId = repository.save(attempt);

        // Assert
        assertEquals(1, countImportRows(sourceName));
        assertEquals(1, countPaymentRows(importId));

        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT
                         payment_imports.source_name,
                         payment_imports.parse_status,
                         payment_imports.description,
                         payments.payment_id,
                         payments.account_id,
                         payments.customer_name,
                         payments.customer_email,
                         payments.amount,
                         payments.currency,
                         payments.payment_status,
                         payments.rejection_status
                     FROM payment_imports
                     JOIN payments ON payments.import_id = payment_imports.id
                     WHERE payment_imports.id = ?
                     """)) {
            preparedStatement.setLong(1, importId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertTrue(resultSet.next());

                assertEquals(sourceName, resultSet.getString("source_name"));
                assertEquals(ParseStatus.SUCCESS.name(), resultSet.getString("parse_status"));
                assertEquals("Document parsed successfully.", resultSet.getString("description"));

                assertEquals("PAY-001", resultSet.getString("payment_id"));
                assertEquals("ACC-001", resultSet.getString("account_id"));
                assertEquals("Adam Zaatar", resultSet.getString("customer_name"));
                assertEquals("adam@example.com", resultSet.getString("customer_email"));
                assertEquals(new BigDecimal("100.00"), resultSet.getBigDecimal("amount"));
                assertEquals("JOD", resultSet.getString("currency"));
                assertEquals(PaymentStatus.PENDING.name(), resultSet.getString("payment_status"));
                assertEquals(RejectionStatus.NONE.name(), resultSet.getString("rejection_status"));

                assertFalse(resultSet.next());
            }
        }
    }

    @Test
    public void givenFailedAttemptWhenSavedThenCreatesImportRowAndNoPaymentRows() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createFailedAttempt(sourceName);

        // Act
        long importId = repository.save(attempt);

        // Assert
        assertEquals(1, countImportRows(sourceName));
        assertEquals(0, countPaymentRows(importId));

        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT parse_status, description
                     FROM payment_imports
                     WHERE id = ?
                     """)) {
            preparedStatement.setLong(1, importId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertTrue(resultSet.next());

                assertEquals(ParseStatus.FAILURE.name(), resultSet.getString("parse_status"));
                assertEquals("Document is blank.", resultSet.getString("description"));

                assertFalse(resultSet.next());
            }
        }
    }

    @Test
    public void givenPartialSuccessfulAttemptWhenSavedThenSavesAcceptedAndRejectedPaymentRows() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createPartialSuccessfulAttempt(sourceName);

        // Act
        long importId = repository.save(attempt);

        // Assert
        assertEquals(1, countImportRows(sourceName));
        assertEquals(2, countPaymentRows(importId));
        assertEquals(1, countPaymentRowsByRejectionStatus(importId, RejectionStatus.NONE));
        assertEquals(1, countPaymentRowsByRejectionStatus(importId, RejectionStatus.MISSING_AMOUNT));
    }

    @Test
    public void givenMissingAmountRecordWhenSavedThenStoresAmountAsNull() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createAttemptWithMissingAmount(sourceName);

        // Act
        long importId = repository.save(attempt);

        // Assert
        assertTrue(amountIsNull(importId, "PAY-001"));
    }

    @Test
    public void givenMissingCurrencyRecordWhenSavedThenStoresCurrencyAsNull() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createAttemptWithMissingCurrency(sourceName);

        // Act
        long importId = repository.save(attempt);

        // Assert
        assertTrue(currencyIsNull(importId, "PAY-001"));
    }

    @Test
    public void givenDuplicatePaymentIdsWhenSavedThenBothPaymentRowsAreStored() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createAttemptWithDuplicatePaymentIds(sourceName);

        // Act
        long importId = repository.save(attempt);

        // Assert
        assertEquals(2, countPaymentRows(importId));
        assertEquals(2, countPaymentRowsByPaymentId(importId, "PAY-001"));
        assertEquals(1, countPaymentRowsByRejectionStatus(importId, RejectionStatus.NONE));
        assertEquals(1, countPaymentRowsByRejectionStatus(importId, RejectionStatus.DUPLICATE_PAYMENT_ID));
    }

    @Test
    public void givenPaymentInsertFailsWhenSavedThenRollsBackImportRow() throws SQLException {
        // Arrange
        String sourceName = createUniqueSourceName();
        JdbcPaymentImportRepository repository = createRepository();
        PaymentImportAttempt attempt = createAttemptWithInvalidCurrency(sourceName);

        // Act and Assert
        assertThrows(
                PersistenceException.class,
                () -> repository.save(attempt)
        );

        assertEquals(0, countImportRows(sourceName));
        assertEquals(0, countPaymentRowsBySourceName(sourceName));
    }

    private PaymentImportAttempt createSuccessfulAttempt(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.SUCCESS,
                "Document parsed successfully.",
                List.of(createAcceptedPaymentRecord("PAY-001"))
        );
    }

    private PaymentImportAttempt createFailedAttempt(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.FAILURE,
                "Document is blank.",
                List.of()
        );
    }

    private PaymentImportAttempt createPartialSuccessfulAttempt(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.PARTIAL_SUCCESS,
                "Document parsed with rejected rows.",
                List.of(
                        createAcceptedPaymentRecord("PAY-001"),
                        createMissingAmountPaymentRecord("PAY-002")
                )
        );
    }

    private PaymentImportAttempt createAttemptWithMissingAmount(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.PARTIAL_SUCCESS,
                "Document parsed with missing amount.",
                List.of(createMissingAmountPaymentRecord("PAY-001"))
        );
    }

    private PaymentImportAttempt createAttemptWithMissingCurrency(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.PARTIAL_SUCCESS,
                "Document parsed with missing currency.",
                List.of(createMissingCurrencyPaymentRecord("PAY-001"))
        );
    }

    private PaymentImportAttempt createAttemptWithDuplicatePaymentIds(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.PARTIAL_SUCCESS,
                "Document parsed with duplicate payment IDs.",
                List.of(
                        createAcceptedPaymentRecord("PAY-001"),
                        createDuplicatePaymentRecord("PAY-001")
                )
        );
    }

    private PaymentImportAttempt createAttemptWithInvalidCurrency(String sourceName) {
        return new PaymentImportAttempt(
                sourceName,
                IMPORTED_AT,
                ParseStatus.SUCCESS,
                "Document parsed successfully.",
                List.of(createInvalidCurrencyPaymentRecord("PAY-001"))
        );
    }

    private PaymentRecord createAcceptedPaymentRecord(String paymentId) {
        PaymentRecord record = new PaymentRecord(
                paymentId,
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JOD",
                PaymentStatus.PENDING
        );

        record.setRejectionStatus(RejectionStatus.NONE);

        return record;
    }

    private PaymentRecord createMissingAmountPaymentRecord(String paymentId) {
        PaymentRecord record = new PaymentRecord(
                paymentId,
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                null,
                "JOD",
                PaymentStatus.PENDING
        );

        record.setRejectionStatus(RejectionStatus.MISSING_AMOUNT);

        return record;
    }

    private PaymentRecord createMissingCurrencyPaymentRecord(String paymentId) {
        PaymentRecord record = new PaymentRecord(
                paymentId,
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "",
                PaymentStatus.PENDING
        );

        record.setRejectionStatus(RejectionStatus.MISSING_CURRENCY);

        return record;
    }

    private PaymentRecord createDuplicatePaymentRecord(String paymentId) {
        PaymentRecord record = new PaymentRecord(
                paymentId,
                "ACC-002",
                "Duplicate Customer",
                "duplicate@example.com",
                new BigDecimal("150.00"),
                "JOD",
                PaymentStatus.PENDING
        );

        record.setRejectionStatus(RejectionStatus.DUPLICATE_PAYMENT_ID);

        return record;
    }

    private PaymentRecord createInvalidCurrencyPaymentRecord(String paymentId) {
        PaymentRecord record = new PaymentRecord(
                paymentId,
                "ACC-001",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JODD",
                PaymentStatus.PENDING
        );

        record.setRejectionStatus(RejectionStatus.NONE);

        return record;
    }

    private int countImportRows(String sourceName) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM payment_imports
                     WHERE source_name = ?
                     """)) {
            preparedStatement.setString(1, sourceName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private int countPaymentRows(long importId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM payments
                     WHERE import_id = ?
                     """)) {
            preparedStatement.setLong(1, importId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private int countPaymentRowsBySourceName(String sourceName) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM payments
                     JOIN payment_imports ON payment_imports.id = payments.import_id
                     WHERE payment_imports.source_name = ?
                     """)) {
            preparedStatement.setString(1, sourceName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private int countPaymentRowsByPaymentId(long importId, String paymentId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM payments
                     WHERE import_id = ?
                     AND payment_id = ?
                     """)) {
            preparedStatement.setLong(1, importId);
            preparedStatement.setString(2, paymentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private int countPaymentRowsByRejectionStatus(long importId, RejectionStatus rejectionStatus) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM payments
                     WHERE import_id = ?
                     AND rejection_status = ?
                     """)) {
            preparedStatement.setLong(1, importId);
            preparedStatement.setString(2, rejectionStatus.name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private boolean amountIsNull(long importId, String paymentId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT amount IS NULL
                     FROM payments
                     WHERE import_id = ?
                     AND payment_id = ?
                     """)) {
            preparedStatement.setLong(1, importId);
            preparedStatement.setString(2, paymentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getBoolean(1);
            }
        }
    }

    private boolean currencyIsNull(long importId, String paymentId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT currency IS NULL
                     FROM payments
                     WHERE import_id = ?
                     AND payment_id = ?
                     """)) {
            preparedStatement.setLong(1, importId);
            preparedStatement.setString(2, paymentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getBoolean(1);
            }
        }
    }

    private void cleanIntegrationTestRows() throws SQLException {
        try (Connection connection = openConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("""
                    DELETE FROM payments
                    WHERE import_id IN (
                        SELECT id
                        FROM payment_imports
                        WHERE source_name LIKE ?
                    )
                    """)) {
                preparedStatement.setString(1, TEST_SOURCE_PREFIX + "%");
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("""
                    DELETE FROM payment_imports
                    WHERE source_name LIKE ?
                    """)) {
                preparedStatement.setString(1, TEST_SOURCE_PREFIX + "%");
                preparedStatement.executeUpdate();
            }
        }
    }

    private String createUniqueSourceName() {
        return TEST_SOURCE_PREFIX + UUID.randomUUID() + ".csv";
    }

    private boolean databaseEnvironmentIsConfigured() {
        return System.getenv("EVENTGUARD_DB_URL") != null
                && !System.getenv("EVENTGUARD_DB_URL").isBlank()
                && System.getenv("EVENTGUARD_DB_USER") != null
                && !System.getenv("EVENTGUARD_DB_USER").isBlank()
                && System.getenv("EVENTGUARD_DB_PASSWORD") != null;
    }

    private JdbcPaymentImportRepository createRepository() {
        ConnectionProvider connectionProvider = new DriverManagerConnectionProvider(
                System.getenv("EVENTGUARD_DB_URL"),
                System.getenv("EVENTGUARD_DB_USER"),
                System.getenv("EVENTGUARD_DB_PASSWORD")
        );

        return new JdbcPaymentImportRepository(connectionProvider);
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                System.getenv("EVENTGUARD_DB_URL"),
                System.getenv("EVENTGUARD_DB_USER"),
                System.getenv("EVENTGUARD_DB_PASSWORD")
        );
    }
}