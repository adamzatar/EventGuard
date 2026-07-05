package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.azaatar.eventguard.domain.RejectionStatus.DUPLICATE_PAYMENT_ID;
import static com.azaatar.eventguard.domain.RejectionStatus.NONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProcessingServiceTest {

    private PaymentRecord paymentRecord(String paymentId) {
        return new PaymentRecord(
                paymentId,
                "ACC-123",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JOD",
                PaymentStatus.PENDING
        );
    }

    private PaymentRecord rejectedDuplicatePaymentRecord(String paymentId) {
        PaymentRecord record = paymentRecord(paymentId);
        record.setRejectionStatus(DUPLICATE_PAYMENT_ID);
        return record;
    }

    @Test
    void givenDuplicatePaymentIdWhenProcessingThenRejectsSecondRecord() {
        // Arrange
        PaymentRecord original = paymentRecord("PAY-001");
        PaymentRecord duplicate = paymentRecord("PAY-001");
        List<PaymentRecord> records = List.of(original, duplicate);

        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(List.of(original), report.getAcceptedRecords());
        assertEquals(List.of(duplicate), report.getRejectedRecords());
        assertEquals(1, report.getRejectedCount());
        assertEquals(DUPLICATE_PAYMENT_ID, report.getRejectedRecords().get(0).getRejectionStatus());
        assertEquals(NONE, report.getAcceptedRecords().get(0).getRejectionStatus());
    }

    @Test
    void givenNullRecordsWhenProcessingThenRejectsInput() {
        // Arrange
        PaymentProcessingService service = new PaymentProcessingService();

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> service.process(null));
    }

    @Test
    void givenUniquePaymentIdsWhenProcessingThenAcceptsAllRecords() {
        // Arrange
        List<PaymentRecord> records = List.of(
                paymentRecord("PAY-001"),
                paymentRecord("PAY-002"),
                paymentRecord("PAY-003")
        );

        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(records, report.getAcceptedRecords());
        assertEquals(List.of(), report.getRejectedRecords());
        assertEquals(3, report.getAcceptedCount());
        assertEquals(0, report.getRejectedCount());
        assertEquals(3, report.getTotalCount());
    }

    @Test
    void givenDuplicatePaymentIdAfterUniqueRecordsWhenProcessingThenRejectsDuplicate() {
        // Arrange
        List<PaymentRecord> records = List.of(
                paymentRecord("PAY-001"),
                paymentRecord("PAY-002"),
                paymentRecord("PAY-001")
        );

        List<PaymentRecord> expectedAccepted = List.of(
                paymentRecord("PAY-001"),
                paymentRecord("PAY-002")
        );

        List<PaymentRecord> expectedRejected = List.of(
                rejectedDuplicatePaymentRecord("PAY-001")
        );

        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(expectedRejected, report.getRejectedRecords());
        assertEquals(expectedAccepted, report.getAcceptedRecords());
        assertEquals(1, report.getRejectedCount());
        assertEquals(2, report.getAcceptedCount());
        assertEquals(3, report.getTotalCount());
    }

    @Test
    void givenMultipleDuplicatesWhenProcessingThenRejectsAllLaterDuplicates() {
        // Arrange
        List<PaymentRecord> records = List.of(
                paymentRecord("PAY-001"),
                paymentRecord("PAY-001"),
                paymentRecord("PAY-001")
        );

        List<PaymentRecord> expectedAccepted = List.of(
                paymentRecord("PAY-001")
        );

        List<PaymentRecord> expectedRejected = List.of(
                rejectedDuplicatePaymentRecord("PAY-001"),
                rejectedDuplicatePaymentRecord("PAY-001")
        );

        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(expectedRejected, report.getRejectedRecords());
        assertEquals(expectedAccepted, report.getAcceptedRecords());
        assertEquals(2, report.getRejectedCount());
        assertEquals(1, report.getAcceptedCount());
        assertEquals(3, report.getTotalCount());
    }

    @Test
    void givenEmptyRecordsWhenProcessingThenReturnsEmptyReport() {
        // Arrange
        List<PaymentRecord> records = List.of();
        List<PaymentRecord> expectedAccepted = List.of();
        List<PaymentRecord> expectedRejected = List.of();

        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(expectedRejected, report.getRejectedRecords());
        assertEquals(expectedAccepted, report.getAcceptedRecords());
        assertEquals(0, report.getRejectedCount());
        assertEquals(0, report.getAcceptedCount());
        assertEquals(0, report.getTotalCount());
    }

}