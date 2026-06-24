package com.azaatar.eventguard.parsing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvPaymentParserTest {


    @Test
    void givenValidPaymentLineWhenParsingThenReturnsPaymentRecord() {

        // Arrange
        List<String> rawLine = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING");
        PaymentRecord expectedLine = new PaymentRecord("PAY-001", "ACC-123", "Adam Zaatar", "adam@example.com", new BigDecimal("100.00"), "JOD", PaymentStatus.PENDING);

        // Act
        PaymentParser parser = new CsvPaymentParser();
        List<PaymentRecord> paymentLines = parser.parse(rawLine);

        // Assert
        assertEquals(1, paymentLines.size());
        assertEquals(expectedLine, paymentLines.getFirst());
    }

    @Test
    void givenListWithNullLineWhenParsingThenRejectsLine() {
        // Arrange and Act
        PaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(null), "Can't parse null list!");

    }

    @Test
    void givenNullLinesListWhenParsingThenRejectsInput() {
        // Arrange
        List<String> nullLine = new ArrayList<>();
        nullLine.add(null);
        nullLine.add("Adam");

        // Act
        PaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(nullLine), "Can't parse list with null value");
    }

    @Test
    void givenBlankLineWhenParsingThenRejectsLine() {

        // Arrange
        List<String> blankLine = new ArrayList<>();
        blankLine.add("  ");

        // Act
        PaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(blankLine), "Can't parse list with blank line");
    }

    @Test
    void givenLineWithTooFewColumnsWhenParsingThenRejectsLine() {

        // Arrange
        List<String> missingFields = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD");
        PaymentParser parser = new CsvPaymentParser();

        // Act and Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(missingFields),
                "Cannot parse a line with missing fields!"
        );


    }

    @Test
    void givenLineWithTooManyColumnsWhenParsingThenRejectsLine() {

        // Arrange
        List<String> extraFields = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING,hi");
        PaymentParser parser = new CsvPaymentParser();

        // Act and Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(extraFields),
                "Cannot parse a line with extra fields!"
        );

    }

    @Test
    void givenInvalidAmountWhenParsingThenRejectsLine() {

        // Arrange
        List<String> invalidAmount = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,not-a-number,JOD,PENDING");

        // Act
        CsvPaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(invalidAmount));
    }

    @Test
    void givenInvalidStatusWhenParsingThenRejectsLine() {

        // Arrange
        List<String> invalidStatus = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,UNKNOWN");

        // Act
        PaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(invalidStatus));
    }

    @Test
    void givenMissingTrailingStatusWhenParsingThenRejectsLine() {

        // Arrange
        List<String> missingTrailingStatus = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,");
        PaymentParser parser = new CsvPaymentParser();

        // Act and Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse(missingTrailingStatus),
                "Cannot parse a line with a missing trailing status field!"
        );

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "   ,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING",
            "PAY-001,   ,Adam Zaatar,adam@example.com,100.00,JOD,PENDING",
            "PAY-001,ACC-123,   ,adam@example.com,100.00,JOD,PENDING"
    })    void givenBlankRequiredFieldWhenParsingThenRejectsLine(String invalidLine) {

        // Act
        PaymentParser parser = new CsvPaymentParser();

        // Assert
        assertThrows(IllegalArgumentException.class, () -> parser.parse(List.of(invalidLine)), "Can't parse line with blank required field");
    }
}