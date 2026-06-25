package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.domain.RejectedPaymentRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.azaatar.eventguard.domain.RejectionReason.DUPLICATE_PAYMENT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProcessingServiceTest {

    private PaymentRecord paymentRecord(String paymentId) {
        return new PaymentRecord(paymentId, "ACC-123", "Adam Zaatar", "adam@example.com", new BigDecimal("100.00"), "JOD", PaymentStatus.PENDING);
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
        assertEquals(1, report.getRejectedCount());
        assertEquals(duplicate, report.getRejectedRecords().getFirst().getPaymentRecord());
        assertEquals(DUPLICATE_PAYMENT_ID, report.getRejectedRecords().getFirst().getReason());

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
        List<PaymentRecord> records = List.of(paymentRecord("PAY-001"), paymentRecord("PAY-002"), paymentRecord("PAY-003"));
        List<RejectedPaymentRecord> rejected = List.of();
        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);
        List<PaymentRecord> acceptedRecords = report.getAcceptedRecords();

        // Assert
        assertEquals(records, acceptedRecords);
        assertEquals(0, report.getRejectedCount());


    }

    @Test
    void givenDuplicatePaymentIdAfterUniqueRecordsWhenProcessingThenRejectsDuplicate() {

        // Arrange
        List<PaymentRecord> records = List.of(paymentRecord("PAY-001"), paymentRecord("PAY-002"), paymentRecord("PAY-001"));
        List<PaymentRecord> expectedAccepted = List.of(paymentRecord("PAY-001"), paymentRecord("PAY-002"));
        List<RejectedPaymentRecord> expectedRejected = List.of(new RejectedPaymentRecord(paymentRecord("PAY-001"), DUPLICATE_PAYMENT_ID));
        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(expectedRejected, report.getRejectedRecords());
        assertEquals(expectedAccepted, report.getAcceptedRecords());
        assertEquals(1, report.getRejectedCount());
        assertEquals(2, report.getAcceptedCount());
    }

    @Test
    void givenMultipleDuplicatesWhenProcessingThenRejectsAllLaterDuplicates() {

        // Arrange
        List<PaymentRecord> records = List.of(paymentRecord("PAY-001"), paymentRecord("PAY-001"), paymentRecord("PAY-001"));
        List<PaymentRecord> expectedAccepted = List.of(paymentRecord("PAY-001"));
        List<RejectedPaymentRecord> expectedRejected = List.of(new RejectedPaymentRecord(paymentRecord("PAY-001"), DUPLICATE_PAYMENT_ID), new RejectedPaymentRecord(paymentRecord("PAY-001"), DUPLICATE_PAYMENT_ID));
        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        PaymentProcessingReport report = service.process(records);

        // Assert
        assertEquals(expectedRejected, report.getRejectedRecords());
        assertEquals(expectedAccepted, report.getAcceptedRecords());
        assertEquals(2, report.getRejectedCount());
        assertEquals(1, report.getAcceptedCount());

    }

    @Test
    void givenEmptyRecordsWhenProcessingThenReturnsEmptyReport() {

        // Arrange
        List<PaymentRecord> records = List.of();
        List<PaymentRecord> expectedAccepted = List.of();
        List<RejectedPaymentRecord> expectedRejected = List.of();
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