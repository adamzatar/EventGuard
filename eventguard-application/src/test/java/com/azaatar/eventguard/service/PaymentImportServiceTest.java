package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.pojo.ParseStatus;
import com.azaatar.eventguard.pojo.PaymentParseResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentImportServiceTest {

    @Test
    void givenReaderReturnsDocumentAndParserReturnsParseResultWhenImportPaymentsThenReturnsParseResult() throws IOException {

        // Arrange
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

        PaymentParseResult expectedResult = new PaymentParseResult(
                ParseStatus.SUCCESS,
                "Document parsed successfully.",
                List.of(expectedRecord)
        );

        PaymentFileReader reader = new FakePaymentFileReader(document);
        FakePaymentParser parser = new FakePaymentParser(expectedResult);
        PaymentImportService service = new PaymentImportService(reader, parser);

        // Act
        PaymentParseResult actualResult = service.importPayments(Path.of("payments.csv"));

        // Assert
        assertEquals(document, parser.getReceivedDocument(), "Parser did not receive the document from the reader!");
        assertEquals(expectedResult, actualResult, "Actual parse result is not equal to expected parse result!");
    }
    @Test
    void givenReaderThrowsIOExceptionWhenImportPaymentsThenPropagatesIOException() {

        // Arrange
        PaymentFileReader reader = new ThrowingPaymentFileReader();
        PaymentParser parser = new FakePaymentParser(null);
        PaymentImportService service = new PaymentImportService(reader, parser);

        // Act and Assert
        assertThrows(IOException.class, () -> service.importPayments(Path.of("payments.csv")));
    }


    @Test
    void givenNullReaderWhenCreatingServiceThenRejectsDependency() {

        // Arrange
        PaymentFileReader reader = null;
        FakePaymentParser parser = new FakePaymentParser(null);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> new PaymentImportService(reader, parser), "Cannot create a service with a null reader!");
    }

    @Test
    void givenNullParserWhenCreatingServiceThenRejectsDependency() {

        // Arrange
        PaymentFileReader reader = new FakePaymentFileReader(null);
        FakePaymentParser parser = null;

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> new PaymentImportService(reader, parser), "Cannot create a service with a null parser!");
    }


    // Test Doubles
    private static class FakePaymentFileReader implements PaymentFileReader {

        private final String document;

        private FakePaymentFileReader(String document) {
            this.document = document;
        }

        @Override
        public String read(Path path) {
            return document;
        }
    }

    private static class ThrowingPaymentFileReader implements PaymentFileReader {

        @Override
        public String read(Path path) throws IOException {
            throw new IOException();
        }
    }

    private static class FakePaymentParser implements PaymentParser {

        private final PaymentParseResult result;
        private String receivedDocument;

        private FakePaymentParser(PaymentParseResult result) {
            this.result = result;
        }

        @Override
        public PaymentParseResult parse(String document) {
            this.receivedDocument = document;
            return result;
        }

        private String getReceivedDocument() {
            return receivedDocument;
        }
    }
}