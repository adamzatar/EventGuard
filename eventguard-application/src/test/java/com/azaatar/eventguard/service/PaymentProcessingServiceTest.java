package com.azaatar.eventguard.service;

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
        List<PaymentRecord> processedRecords = service.process(records);

        // Assert
        assertEquals(2, processedRecords.size());
        assertEquals(records, processedRecords);
        assertEquals(NONE, original.getRejectionStatus());
        assertEquals(DUPLICATE_PAYMENT_ID, duplicate.getRejectionStatus());
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
        List<PaymentRecord> processedRecords = service.process(records);

        // Assert
        assertEquals(records, processedRecords);
        assertEquals(NONE, processedRecords.get(0).getRejectionStatus());
        assertEquals(NONE, processedRecords.get(1).getRejectionStatus());
        assertEquals(NONE, processedRecords.get(2).getRejectionStatus());
    }

    @Test
    void givenDuplicatePaymentIdAfterUniqueRecordsWhenProcessingThenRejectsDuplicate() {
        // Arrange
        List<PaymentRecord> records = List.of(
                paymentRecord("PAY-001"),
                paymentRecord("PAY-002"),
                paymentRecord("PAY-001")
        );


        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        List<PaymentRecord> processedRecords = service.process(records);

        // Assert
        assertEquals(NONE, processedRecords.get(0).getRejectionStatus());
        assertEquals(NONE, processedRecords.get(1).getRejectionStatus());
        assertEquals(DUPLICATE_PAYMENT_ID, processedRecords.get(2).getRejectionStatus());
    }

    @Test
    void givenMultipleDuplicatesWhenProcessingThenRejectsAllLaterDuplicates() {
        // Arrange
        List<PaymentRecord> records = List.of(
                paymentRecord("PAY-001"),
                paymentRecord("PAY-001"),
                paymentRecord("PAY-001")
        );

        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        List<PaymentRecord> processedRecords = service.process(records);

        // Assert
        assertEquals(NONE, processedRecords.get(0).getRejectionStatus());
        assertEquals(DUPLICATE_PAYMENT_ID, processedRecords.get(1).getRejectionStatus());
        assertEquals(DUPLICATE_PAYMENT_ID, processedRecords.get(2).getRejectionStatus());
    }

    @Test
    void givenEmptyRecordsWhenProcessingThenReturnsEmptyList() {
        // Arrange
        List<PaymentRecord> records = List.of();
        PaymentProcessingService service = new PaymentProcessingService();

        // Act
        List<PaymentRecord> processedRecords = service.process(records);

        // Assert
        assertEquals(List.of(), processedRecords);
    }

}