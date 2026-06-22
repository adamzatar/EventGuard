package com.azaatar.eventguard.ingestion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NioPaymentFileReaderTest {

    @TempDir
    private Path tempDir;

    @Test
    void givenValidFilePathWhenReadLinesThenReturnsFileLines() throws IOException {

        // Arrange
        Path file = tempDir.resolve("payments.csv");
        List<String> expectedLines = List.of(
                "header",
                "line one",
                "line two"
        );
        Files.write(file, expectedLines);
        NioPaymentFileReader reader = new NioPaymentFileReader();

        // Act
        List<String> actualLines = reader.readLines(file);

        // Assert
        assertEquals(expectedLines, actualLines, "Reader should return the exact file lines in order");
    }

    @Test
    void givenMissingFilePathWhenReadLinesThenThrowsIOException() {

        // Arrange
        Path missingFile = tempDir.resolve("missing-payments.csv");
        NioPaymentFileReader reader = new NioPaymentFileReader();

        // Act and Assert
        assertThrows(IOException.class, () -> reader.readLines(missingFile));
    }
}
