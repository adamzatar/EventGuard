package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.PaymentParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentImportServiceTest {

    @Test
    void givenReaderReturnsLinesAndParserReturnsRecordsWhenImportPaymentsThenReturnsParsedRecords() throws IOException {

        // Arrange
        List<String> rawLines = List.of("PAY-001,ACC-123,Adam Zaatar,adam@example.com,100.00,JOD,PENDING");

        List<PaymentRecord> expectedRecords = List.of(new PaymentRecord("PAY-001", "ACC-123", "Adam Zaatar", "adam@example.com", new BigDecimal("100.00"), "JOD", PaymentStatus.PENDING));

        PaymentFileReader reader = new FakePaymentFileReader(rawLines);
        FakePaymentParser parser = new FakePaymentParser(expectedRecords);
        PaymentImportService service = new PaymentImportService(reader, parser);

        // Act
        List<PaymentRecord> actualRecords = service.importPayments(Path.of("payments.csv"));

        // Assert
        assertEquals(rawLines, parser.getReceivedLines(), "Raw lines aren't equal to lines received by parser!");
        assertEquals(expectedRecords, actualRecords, "Actual records aren't equal to expected records!");

    }

    @Test
    void givenReaderThrowsIOExceptionWhenImportPaymentsThenPropagatesIOException() {

        // Arrange
        PaymentFileReader reader = new ThrowingPaymentFileReader();
        PaymentParser parser = new FakePaymentParser(List.of());
        PaymentImportService service = new PaymentImportService(reader, parser);

        // Act and Assert
        assertThrows(IOException.class, () -> service.importPayments(Path.of("payments.csv")));
    }


    @Test
    void givenNullReaderWhenCreatingServiceThenRejectsDependency() {

        // Arrange
        PaymentFileReader reader = null;
        FakePaymentParser parser = new FakePaymentParser(List.of());

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> new PaymentImportService(reader, parser), "Cannot create a service with a null reader!");
    }

    @Test
    void givenNullParserWhenCreatingServiceThenRejectsDependency() {

        // Arrange
        PaymentFileReader reader = new FakePaymentFileReader(List.of());
        FakePaymentParser parser = null;

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> new PaymentImportService(reader, parser), "Cannot create a service with a null parser!");
    }


    // Test Doubles
    private static class FakePaymentFileReader implements PaymentFileReader {
        private final List<String> linesToReturn;

        private FakePaymentFileReader(List<String> linesToReturn) {
            this.linesToReturn = linesToReturn;
        }

        @Override
        public List<String> readLines(Path path) throws IOException {
            return linesToReturn;
        }
    }

    private static class ThrowingPaymentFileReader implements PaymentFileReader {
        @Override
        public List<String> readLines(Path path) throws IOException {
            throw new IOException();
        }
    }

    private static class FakePaymentParser implements PaymentParser {
        private final List<PaymentRecord> recordsToReturn;
        private List<String> receivedLines;

        private FakePaymentParser(List<PaymentRecord> recordsToReturn) {
            this.recordsToReturn = recordsToReturn;
        }

        @Override
        public List<PaymentRecord> parse(List<String> lines) {
            this.receivedLines = lines;
            return recordsToReturn;
        }

        public List<String> getReceivedLines() {
            return receivedLines;
        }
    }
}