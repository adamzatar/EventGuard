package com.azaatar.eventguard.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioPaymentFileReaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void givenValidFilePathWhenReadThenReturnsFileDocument() throws IOException {

        // Arrange
        Path file = tempDir.resolve("payments.csv");
        String expectedDocument = String.join(System.lineSeparator(),
                "header",
                "line one",
                "line two"
        );

        Files.writeString(file, expectedDocument);

        NioPaymentFileReader reader = new NioPaymentFileReader();

        // Act
        String actualDocument = reader.read(file);

        // Assert
        assertEquals(expectedDocument, actualDocument, "Reader should return the exact file document content");
    }

    @Test
    void givenMissingFilePathWhenReadThenThrowsIOException() {

        // Arrange
        Path missingFile = tempDir.resolve("missing-payments.csv");
        NioPaymentFileReader reader = new NioPaymentFileReader();

        // Act and Assert
        assertThrows(IOException.class, () -> reader.read(missingFile));
    }

//    @Test
//    void givenNullPathWhenImportPaymentsThenRejectsPath() {
//
//        // Arrange
//        PaymentFileReader reader = new FakePaymentFileReader(List.of());
//        FakePaymentParser parser = new FakePaymentParser(List.of());
//        PaymentImportService service = new PaymentImportService(reader, parser);
//
//        // Act and Assert
//        assertThrows(IllegalArgumentException.class, () -> service.importPayments(null), "Can't import payments from file with null path!");
//    }

}
