package com.azaatar.eventguard.parsing;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        List<String> rawLine = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING");
        PaymentRecord expectedLine = new PaymentRecord("PAY-001", "ACC-123", "Adam Zaatar", "adam@example.com", new BigDecimal("100.00"), "JOD", PaymentStatus.PENDING);

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
        nullLine.add(null);
        nullLine.add("Adam");

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

    @Test
    void givenRawCsvLineWithMissingFieldsWhenParsingThenRejectsLine() {

        // Arrange
        List<String> missingFields = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD");
        int expectedColumns = CsvPaymentParser.getExpectedColumnCount();
        int actualColumns = missingFields.getFirst().split(",", -1).length;
        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertNotEquals(expectedColumns, actualColumns, "Expected columns isn't equal to actual columns");
        assertThrows(IllegalArgumentException.class, () -> parser.parse(missingFields), "Can't parse if payment line is missing columns");
        assertThat(actualColumns).withFailMessage("This line has %d columns, which is not less than %d", actualColumns, expectedColumns).isLessThan(expectedColumns);
    }

    @Test
    void givenRawCsvLineWithExtraFieldsWhenParsingThenRejectsLine() {

        // Arrange
        List<String> extraFields = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING,hi");
        int expectedColumns = CsvPaymentParser.getExpectedColumnCount();
        int actualColumns = extraFields.getFirst().split(",", -1).length;
        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertNotEquals(expectedColumns, actualColumns, "Expected columns isn't equal to actual columns");
        assertThrows(IllegalArgumentException.class, () -> parser.parse(extraFields), "Can't parse if payment line has extra columns");
        assertThat(actualColumns).withFailMessage("This line has %d columns, which is not more than %d", actualColumns, expectedColumns).isGreaterThan(expectedColumns);
    }

    @Test
    void rejectsInvalidAmount() {

        // Arrange
        List<String> invalidAmount = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,not-a-number,JOD,PENDING");

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(invalidAmount));
    }

    @Test
    void rejectsInvalidStatus() {

        // Arrange
        List<String> invalidStatus = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,UNKNOWN");

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(invalidStatus));
    }

    @Test
    void rejectsMissingTrailingStatus() {

        // Arrange
        List<String> missingTrailingStatus = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,");

        // Act
        int actualColumns = missingTrailingStatus.getFirst().split(",", -1).length;
        int expectedColumns = CsvPaymentParser.getExpectedColumnCount();
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertEquals(expectedColumns, actualColumns);
        assertThrows(IllegalArgumentException.class, () -> parser.parse(missingTrailingStatus), "Can't parse line with missing trailing status");
    }

    @Test
    void rejectsBlankRequiredField() {

        // Arrange
        List<String> blankPaymentId = List.of("   ,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING");

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(blankPaymentId), "Can't parse line with blank required field");
    }
}