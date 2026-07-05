package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

import static com.azaatar.eventguard.domain.RejectionStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class FilePaymentReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void givenValidReportWhenWritingThenCreatesReportFile() throws IOException {
        // Arrange
        PaymentProcessingReport report = validReport();

        Path outputPath = tempDir.resolve("reports").resolve("payment-processing-report.txt");

        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act
        writer.write(report, outputPath);

        // Assert
        assertTrue(Files.exists(outputPath));
    }

    @Test
    void givenValidReportWhenWritingThenWritesExpectedSectionHeadings() throws IOException {
        // Arrange
        PaymentProcessingReport report = validReport();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act
        writer.write(report, outputPath);

        // Assert
        String content = Files.readString(outputPath);

        assertTrue(content.contains("Accepted Payments"));
        assertTrue(content.contains("Rejected Payments: Missing Currency"));
        assertTrue(content.contains("Rejected Payments: Missing Amount"));
        assertTrue(content.contains("Other Rejected Payments"));
        assertTrue(content.contains("Accepted Payments With Amount Greater Than 10"));
    }

    @Test
    void givenAcceptedRecordWithAmountGreaterThanTenWhenWritingThenIncludesRecordInAmountThresholdSection() throws IOException {
        // Arrange
        PaymentProcessingReport report = validReport();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act
        writer.write(report, outputPath);

        // Assert
        String content = Files.readString(outputPath);

        assertTrue(content.contains("Accepted Payments With Amount Greater Than 10"));
        assertTrue(content.contains("PAY-001"));
        assertTrue(content.contains("100.00"));
    }

    @Test
    void givenValidReportWhenWritingThenWritesFooter() throws IOException {
        // Arrange
        PaymentProcessingReport report = validReport();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act
        writer.write(report, outputPath);

        // Assert
        String content = Files.readString(outputPath);

        assertTrue(content.contains("Adam Zaatar"));
    }

    @Test
    void givenValidReportWhenWritingThenWritesPaymentRecords() throws IOException {
        // Arrange
        PaymentProcessingReport report = validReport();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act
        writer.write(report, outputPath);

        // Assert
        String content = Files.readString(outputPath);

        assertTrue(content.contains("PAY-001"));
        assertTrue(content.contains("PAY-002"));
        assertTrue(content.contains("PAY-003"));
        assertTrue(content.contains("ACC-999"));
        assertTrue(content.contains("MISSING_CURRENCY"));
        assertTrue(content.contains("MISSING_AMOUNT"));
        assertTrue(content.contains("DUPLICATE_PAYMENT_ID"));
    }

    @Test
    void givenNullReportWhenWritingThenRejectsInput() throws IOException {
        // Arrange
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> writer.write(null, outputPath));
    }

    @Test
    void givenNullOutputPathWhenWritingThenRejectsInput() throws IOException {
        // Arrange
        PaymentProcessingReport report = validReport();
        FilePaymentReportWriter writer = new FilePaymentReportWriter();

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> writer.write(report, null));
    }

    // constructor in PaymentProcessingReport uses List.of() which cant take null
//    @Test
//    void givenReportWithNullAcceptedRecordWhenWritingThenRejectsInput() throws IOException {
//        // Arrange
//        List<PaymentRecord> acceptedRecords = new ArrayList<>();
//        acceptedRecords.add(null);
//        PaymentProcessingReport report = new PaymentProcessingReport(acceptedRecords, List.of());
//        FilePaymentReportWriter writer = new FilePaymentReportWriter();
//
//        Path outputPath = tempDir.resolve("payment-processing-report.txt");
//
//        // Act
//        writer.write(report, outputPath);
//
//        // Assert
//        assertThrows(NullPointerException.class, () -> writer.write(report, outputPath));
//    }
//
//    @Test
//    void givenReportWithNullRejectedRecordWhenWritingThenRejectsInput() throws IOException {
//        // Arrange
//        List<PaymentRecord> rejectedRecords = new ArrayList<>();
//        rejectedRecords.add(null);
//        PaymentProcessingReport report = new PaymentProcessingReport(List.of(), rejectedRecords);
//        FilePaymentReportWriter writer = new FilePaymentReportWriter();
//
//        Path outputPath = tempDir.resolve("payment-processing-report.txt");
//
//        // Act
//        writer.write(report, outputPath);
//
//        // Assert
//        assertThrows(NullPointerException.class, () -> writer.write(report, outputPath));
//    }


    private PaymentProcessingReport validReport() {
        PaymentRecord accepted = new PaymentRecord("PAY-001", "ACC-123", "Adam Zaatar", "adam@example.com", new BigDecimal("100.00"), "JOD", PaymentStatus.PENDING);
        accepted.setRejectionStatus(NONE);

        PaymentRecord missingCurrency = new PaymentRecord("PAY-002", "ACC-456", "Lina Haddad", "lina@example.com", new BigDecimal("50.00"), "", PaymentStatus.PENDING);
        missingCurrency.setRejectionStatus(MISSING_CURRENCY);

        PaymentRecord missingAmount = new PaymentRecord("PAY-003", "ACC-789", "Omar Saleh", "omar@example.com", BigDecimal.ZERO, "JOD", PaymentStatus.PENDING);
        missingAmount.setRejectionStatus(MISSING_AMOUNT);

        PaymentRecord duplicate = new PaymentRecord("PAY-001", "ACC-999", "Nour Khaled", "nour@example.com", new BigDecimal("25.00"), "JOD", PaymentStatus.PENDING);
        duplicate.setRejectionStatus(DUPLICATE_PAYMENT_ID);

        return new PaymentProcessingReport(List.of(accepted), List.of(missingCurrency, missingAmount, duplicate));
    }
}