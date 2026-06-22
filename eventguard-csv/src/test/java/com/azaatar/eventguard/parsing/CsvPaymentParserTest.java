package com.azaatar.eventguard.parsing;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.parsing.CsvPaymentParser;
import org.junit.jupiter.api.Test;

class CsvPaymentParserTest {
/*
parsesValidPaymentLinesIntoPaymentRecords
rejectsNullLinesList
rejectsNullLine
rejectsBlankLine
rejectsLineWithTooFewColumns
rejectsLineWithTooManyColumns
rejectsInvalidAmount
rejectsMissingTrailingStatus
rejectsBlankRequiredField
 */

    // check how record writes equals and implements it, make it a class with setters, getters and equals
    @Test
    void parsesValidPaymentLinesIntoPaymentRecords() {

        // Arrange
        List<String> rawLine = List.of(
                "PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING"
        );
        PaymentRecord expectedLine = new PaymentRecord(
                "PAY-001",
                "ACC-123",
                "Adam Zaatar",
                "adam@example.com",
                new BigDecimal("100.00"),
                "JOD",
                PaymentStatus.PENDING
        );

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();
        List<PaymentRecord> paymentLines = parser.parse(rawLine);

        // Assert
        assertEquals(1, paymentLines.size());
        assertEquals(expectedLine, paymentLines.getFirst());
    }

    @Test
    void rejectNullLinesList() {
        // Arrange and Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null), "Can't parse null list!");

    }
    @Test
    void rejectNullLine() {
        // Arrange
        List<String> nullLine = new ArrayList<>();
        nullLine.add("Adam");
        nullLine.add(null);

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(nullLine), "Can't parse list with null value");
    }
    @Test
    void givenBlankLineWhenParsingThenRejectsLine() {

        // Arrange
        List<String> blankLine = new ArrayList<>();
        blankLine.add("  ");

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(blankLine), "Can't parse list with blank line");
    }

    // @Test
    void givenPaymentRecordsWithMissingFieldsWhenParsingThenRejectsLine() {

    }
}