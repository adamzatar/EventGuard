package com.azaatar.eventguard.parsing;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.domain.RejectionStatus;
import com.azaatar.eventguard.pojo.ParseStatus;
import com.azaatar.eventguard.pojo.PaymentParseResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvPaymentParserTest {

    private final PaymentParser parser = new CsvPaymentParser();

    @Test
    void givenValidPaymentDocumentWhenParsingThenReturnsSuccessWithPaymentRecord() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING";

        PaymentRecord expectedRecord = new PaymentRecord(
                "PAY-001",
                "ACC-123",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JOD",
                PaymentStatus.PENDING
        );

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.SUCCESS, result.getParseStatus());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecords().size());
        assertEquals(expectedRecord, result.getRecords().get(0));
    }

    @Test
    void givenNullDocumentWhenParsingThenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null));
    }

    @Test
    void givenBlankDocumentWhenParsingThenReturnsFailure() {
        PaymentParseResult result = parser.parse("   ");

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void givenLineWithTooFewColumnsWhenParsingThenReturnsFailure() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void givenLineWithTooManyColumnsWhenParsingThenReturnsFailure() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING,EXTRA";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void givenInvalidNonBlankAmountWhenParsingThenReturnsFailure() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,not-a-number,JOD,PENDING";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void givenInvalidStatusWhenParsingThenReturnsFailure() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,UNKNOWN";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void givenMissingTrailingStatusWhenParsingThenReturnsFailure() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "   ,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING",
            "PAY-001,   ,Adam Zaatar,adam@example.com,100.00,JOD,PENDING",
            "PAY-001,ACC-123,   ,adam@example.com,100.00,JOD,PENDING",
            "PAY-001,ACC-123,Adam Zaatar,   ,100.00,JOD,PENDING",
            "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,   "
    })
    void givenBlankFatalRequiredFieldWhenParsingThenReturnsFailure(String document) {
        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.FAILURE, result.getParseStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void givenMissingAmountWhenParsingThenReturnsPartialSuccessWithMissingAmountRejection() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,,JOD,PENDING";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.PARTIAL_SUCCESS, result.getParseStatus());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecords().size());

        PaymentRecord record = result.getRecords().get(0);
        assertNull(record.getAmount());
        assertEquals("JOD", record.getCurrency());
        assertEquals(RejectionStatus.MISSING_AMOUNT, record.getRejectionStatus());
    }

    @Test
    void givenMissingCurrencyWhenParsingThenReturnsPartialSuccessWithMissingCurrencyRejection() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,,PENDING";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.PARTIAL_SUCCESS, result.getParseStatus());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecords().size());

        PaymentRecord record = result.getRecords().get(0);
        assertEquals(new BigDecimal("100.00"), record.getAmount());
        assertEquals("", record.getCurrency());
        assertEquals(RejectionStatus.MISSING_CURRENCY, record.getRejectionStatus());
    }

    @Test
    void givenMissingAmountAndCurrencyWhenParsingThenMissingAmountTakesPriority() {
        String document = "PAY-001,ACC-123,Adam Zaatar,adam@example.com,,,PENDING";

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.PARTIAL_SUCCESS, result.getParseStatus());
        assertTrue(result.isSuccess());
        assertEquals(1, result.getRecords().size());

        PaymentRecord record = result.getRecords().get(0);
        assertNull(record.getAmount());
        assertEquals("", record.getCurrency());
        assertEquals(RejectionStatus.MISSING_AMOUNT, record.getRejectionStatus());
    }

    @Test
    void givenDocumentWithValidAndParserRejectedRowsWhenParsingThenReturnsPartialSuccess() {
        String document = String.join(System.lineSeparator(),
                "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING",
                "PAY-002,ACC-456,Lina Zaatar,lina@example.com,,JOD,PENDING",
                "PAY-003,ACC-789,Omar Zaatar,omar@example.com,50.00,,PENDING"
        );

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.PARTIAL_SUCCESS, result.getParseStatus());
        assertTrue(result.isSuccess());
        assertEquals(3, result.getRecords().size());

        assertEquals(RejectionStatus.NONE, result.getRecords().get(0).getRejectionStatus());
        assertEquals(RejectionStatus.MISSING_AMOUNT, result.getRecords().get(1).getRejectionStatus());
        assertEquals(RejectionStatus.MISSING_CURRENCY, result.getRecords().get(2).getRejectionStatus());
    }

    @Test
    void givenValidMultiLineDocumentWhenParsingThenReturnsSuccessWithAllRecords() {
        String document = String.join(System.lineSeparator(),
                "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING",
                "PAY-002,ACC-456,Lina Zaatar,lina@example.com,200.00,USD,COMPLETED"
        );

        PaymentParseResult result = parser.parse(document);

        assertEquals(ParseStatus.SUCCESS, result.getParseStatus());
        assertTrue(result.isSuccess());
        assertEquals(2, result.getRecords().size());
        assertEquals(RejectionStatus.NONE, result.getRecords().get(0).getRejectionStatus());
        assertEquals(RejectionStatus.NONE, result.getRecords().get(1).getRejectionStatus());
    }
}