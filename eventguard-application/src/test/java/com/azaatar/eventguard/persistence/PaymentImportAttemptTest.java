package com.azaatar.eventguard.persistence;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.pojo.ParseStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentImportAttemptTest {

    @Test
    public void givenValidSuccessAttemptWhenCreatedThenStoresFields() {
        // Arrange and Act
        PaymentImportAttempt successfulAttempt = new PaymentImportAttempt(
                "payments.csv",
                LocalDateTime.now(),
                ParseStatus.SUCCESS,
                "Document parsed successfully.",
                List.of(createPaymentRecord()));
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