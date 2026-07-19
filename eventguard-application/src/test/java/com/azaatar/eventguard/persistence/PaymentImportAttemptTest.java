package com.azaatar.eventguard.persistence;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.pojo.ParseStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.azaatar.eventguard.pojo.ParseStatus.FAILURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentImportAttemptTest {

    @Test
    public void givenValidSuccessAttemptWhenCreatedThenStoresFields() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act
        PaymentImportAttempt successfulAttempt = new PaymentImportAttempt(
                sourceName,
                importedAt,
                parseStatus,
                description,
                records
        );

        // Assert
        assertEquals(sourceName, successfulAttempt.getSourceName());
        assertEquals(importedAt, successfulAttempt.getImportedAt());
        assertEquals(parseStatus, successfulAttempt.getParseStatus());
        assertEquals(description, successfulAttempt.getDescription());
        assertEquals(records, successfulAttempt.getRecords());
    }

    @Test
    public void givenFailureAttemptWithEmptyRecordsWhenCreatedThenStoresAttempt() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = FAILURE;
        String description = "Document parsing failed at line 1.";
        List<PaymentRecord> records = List.of();

        // Act
        PaymentImportAttempt failureAttempt = new PaymentImportAttempt(
                sourceName,
                importedAt,
                parseStatus,
                description,
                records
        );

        // Assert
        assertEquals(parseStatus, failureAttempt.getParseStatus());
        assertEquals(description, failureAttempt.getDescription());
        assertTrue(failureAttempt.getRecords().isEmpty());
    }

    @Test
    public void givenFailureAttemptWithRecordsWhenCreatedThenThrowsIllegalArgumentException() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = FAILURE;
        String description = "Document parsing failed at line 1.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenBlankSourceNameWhenCreatedThenThrowsIllegalArgumentException() {
        // Arrange
        String sourceName = "   ";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenNullSourceNameWhenCreatedThenThrowsIllegalArgumentException() {
        // Arrange
        String sourceName = null;
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenSourceNameWithWhitespaceWhenCreatedThenStoresTrimmedSourceName() {
        // Arrange
        String sourceName = "  payments.csv  ";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act
        PaymentImportAttempt attempt = new PaymentImportAttempt(
                sourceName,
                importedAt,
                parseStatus,
                description,
                records
        );

        // Assert
        assertEquals("payments.csv", attempt.getSourceName());
    }

    @Test
    public void givenBlankDescriptionWhenCreatedThenThrowsIllegalArgumentException() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "   ";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenNullDescriptionWhenCreatedThenThrowsIllegalArgumentException() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = null;
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenDescriptionWithWhitespaceWhenCreatedThenStoresTrimmedDescription() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "  Document parsed successfully.  ";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act
        PaymentImportAttempt attempt = new PaymentImportAttempt(
                sourceName,
                importedAt,
                parseStatus,
                description,
                records
        );

        // Assert
        assertEquals("Document parsed successfully.", attempt.getDescription());
    }

    @Test
    public void givenNullImportedAtWhenCreatedThenThrowsNullPointerException() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = null;
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(NullPointerException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenNullParseStatusWhenCreatedThenThrowsNullPointerException() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = null;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = List.of(createPaymentRecord());

        // Act and Assert
        assertThrows(NullPointerException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenNullRecordsWhenCreatedThenThrowsNullPointerException() {
        // Arrange
        String sourceName = "payments.csv";
        LocalDateTime importedAt = LocalDateTime.of(2026, 7, 19, 10, 0);
        ParseStatus parseStatus = ParseStatus.SUCCESS;
        String description = "Document parsed successfully.";
        List<PaymentRecord> records = null;

        // Act and Assert
        assertThrows(NullPointerException.class,
                () -> new PaymentImportAttempt(sourceName, importedAt, parseStatus, description, records));
    }

    @Test
    public void givenAttemptWhenModifyingReturnedRecordsThenThrowsUnsupportedOperationException() {
        // Arrange
        PaymentImportAttempt attempt = new PaymentImportAttempt(
                "payments.csv",
                LocalDateTime.of(2026, 7, 19, 10, 0),
                ParseStatus.SUCCESS,
                "Document parsed successfully.",
                List.of(createPaymentRecord())
        );

        // Act and Assert
        assertThrows(UnsupportedOperationException.class,
                () -> attempt.getRecords().add(createPaymentRecord()));
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
}