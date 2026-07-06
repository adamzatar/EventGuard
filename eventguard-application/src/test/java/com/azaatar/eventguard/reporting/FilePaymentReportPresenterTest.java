package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.azaatar.eventguard.domain.RejectionStatus.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilePaymentReportPresenterTest {

    @TempDir
    Path tempDir;

    @Test
    void givenValidReportWhenWritingThenCreatesReportFile() throws IOException {
        // Arrange
        List<PaymentRecord> records = processedRecords();

        Path outputPath = tempDir.resolve("reports").resolve("payment-processing-report.txt");

        FilePaymentReportPresenter presenter = new FilePaymentReportPresenter(outputPath);

        // Act
        presenter.present(records);

        // Assert
        assertTrue(Files.exists(outputPath));
    }

    @Test
    void givenValidReportWhenWritingThenWritesExpectedSectionHeadings() throws IOException {
        // Arrange
        List<PaymentRecord> records = processedRecords();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportPresenter presenter = new FilePaymentReportPresenter(outputPath);

        // Act
        presenter.present(records);

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
        List<PaymentRecord> records = processedRecords();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportPresenter presenter = new FilePaymentReportPresenter(outputPath);

        // Act
        presenter.present(records);

        // Assert
        String content = Files.readString(outputPath);

        assertTrue(content.contains("Accepted Payments With Amount Greater Than 10"));
        assertTrue(content.contains("PAY-001"));
        assertTrue(content.contains("100.00"));
    }

    @Test
    void givenValidReportWhenWritingThenWritesFooter() throws IOException {
        // Arrange
        List<PaymentRecord> records = processedRecords();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportPresenter presenter = new FilePaymentReportPresenter(outputPath);

        // Act
        presenter.present(records);

        // Assert
        String content = Files.readString(outputPath);

        assertTrue(content.contains("Adam Zaatar"));
    }

    @Test
    void givenValidReportWhenWritingThenWritesPaymentRecords() throws IOException {
        // Arrange
        List<PaymentRecord> records = processedRecords();
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportPresenter presenter = new FilePaymentReportPresenter(outputPath);

        // Act
        presenter.present(records);

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
    void givenNullPaymentRecordsListWhenWritingThenRejectsInput() throws IOException {
        // Arrange
        Path outputPath = tempDir.resolve("payment-processing-report.txt");
        FilePaymentReportPresenter presenter = new FilePaymentReportPresenter(outputPath);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> presenter.present(null));
    }

    @Test
    void givenNullOutputPathWhenCreatingPresenterThenRejectsInput() throws IOException {
        // Arrange, Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> new FilePaymentReportPresenter(null));
    }


    private List<PaymentRecord> processedRecords() {
        PaymentRecord accepted = new PaymentRecord("PAY-001", "ACC-123", "Adam Zaatar", "adam@example.com", new BigDecimal("100.00"), "JOD", PaymentStatus.PENDING);
        accepted.setRejectionStatus(NONE);

        PaymentRecord missingCurrency = new PaymentRecord("PAY-002", "ACC-456", "Lina Haddad", "lina@example.com", new BigDecimal("50.00"), "", PaymentStatus.PENDING);
        missingCurrency.setRejectionStatus(MISSING_CURRENCY);

        PaymentRecord missingAmount = new PaymentRecord("PAY-003", "ACC-789", "Omar Saleh", "omar@example.com", BigDecimal.ZERO, "JOD", PaymentStatus.PENDING);
        missingAmount.setRejectionStatus(MISSING_AMOUNT);

        PaymentRecord duplicate = new PaymentRecord("PAY-001", "ACC-999", "Nour Khaled", "nour@example.com", new BigDecimal("25.00"), "JOD", PaymentStatus.PENDING);
        duplicate.setRejectionStatus(DUPLICATE_PAYMENT_ID);

        return List.of(accepted, missingCurrency, missingAmount, duplicate);
    }
}