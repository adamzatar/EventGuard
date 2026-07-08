package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.azaatar.eventguard.domain.RejectionStatus.*;

// TODO: one interface for printer, formatter, etc different forms of output/presentation
// TODO: all classes should implement same interface

public class FilePaymentReportPresenter implements PaymentReportPresenter {

    private static final BigDecimal AMOUNT_THRESHOLD = new BigDecimal("10");
    private static final Path DEFAULT_OUTPUT_PATH =
            Path.of("reports/payment-processing-report.txt");

    private final Path outputPath;

    public FilePaymentReportPresenter() {
        this(DEFAULT_OUTPUT_PATH);
    }

    FilePaymentReportPresenter(Path outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path must not be null");
        }

        this.outputPath = outputPath;
    }

    public void present(List<PaymentRecord> processedRecords) throws IOException {
        if (processedRecords == null) {
            throw new IllegalArgumentException("Processed records must not be null");
        }

        validateRecords(processedRecords);

        List<PaymentRecord> acceptedRecords = processedRecords.stream()
                .filter(record -> record.getRejectionStatus() == NONE)
                .toList();

        List<PaymentRecord> rejectedRecords = processedRecords.stream()
                .filter(record -> record.getRejectionStatus() != NONE)
                .toList();

        ensureParentDirectoryExists();
        StringBuilder builder = new StringBuilder();

        appendSection(builder, "Accepted Payments", acceptedRecords);
        appendSection(builder, "Rejected Payments: Missing Currency", rejectedMissingCurrency(rejectedRecords));
        appendSection(builder, "Rejected Payments: Missing Amount", rejectedMissingAmount(rejectedRecords));
        appendSection(builder, "Other Rejected Payments", otherRejected(rejectedRecords));
        appendSection(builder, "Accepted Payments With Amount Greater Than 10", acceptedAmountGreaterThanTen(acceptedRecords));

        builder.append(System.lineSeparator());
        builder.append("Report written by Adam Zaatar.").append(System.lineSeparator());

        Files.writeString(this.outputPath, builder.toString());

    }
    private void validateRecords(List<PaymentRecord> processedRecords) {
        if (processedRecords == null) {
            throw new IllegalArgumentException("Processed records must not be null");
        }

        if(processedRecords.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Processed records must not contain null records");
        }
    }

    private void ensureParentDirectoryExists() throws IOException {
        Path parent = this.outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void appendSection(StringBuilder builder, String title, List<PaymentRecord> records) {
        builder.append("=== ").append(title).append(" ===").append(System.lineSeparator());

        if (records.isEmpty()) {
            builder.append("No records found!").append(System.lineSeparator()).append(System.lineSeparator());
            return;
        }

        for (PaymentRecord record : records) {
            builder.append(formatRecord(record)).append(System.lineSeparator());
        }

        builder.append(System.lineSeparator());
    }

    private String formatRecord(PaymentRecord record) {

        return "paymentId=" + record.getPaymentId()
                + ", accountId=" + record.getAccountId()
                + ", name=" + record.getName()
                + ", email=" + record.getEmail()
                + ", amount=" + record.getAmount()
                + ", currency=" + record.getCurrency()
                + ", paymentStatus=" + record.getStatus()
                + ", rejectionStatus=" + record.getRejectionStatus();

    }

    private List<PaymentRecord> rejectedMissingCurrency(List<PaymentRecord> rejectedRecords) {
        return rejectedRecords.stream()
                .filter(record -> record.getRejectionStatus() == MISSING_CURRENCY)
                .toList();
    }

    private List<PaymentRecord> rejectedMissingAmount(List<PaymentRecord> rejectedRecords) {
        return rejectedRecords.stream()
                .filter(record -> record.getRejectionStatus() == MISSING_AMOUNT)
                .toList();
    }

    private List<PaymentRecord> otherRejected(List<PaymentRecord> rejectedRecords) {
        return rejectedRecords.stream()
                .filter(record -> record.getRejectionStatus() != MISSING_CURRENCY)
                .filter(record -> record.getRejectionStatus() != MISSING_AMOUNT)
                .toList();
    }

    private List<PaymentRecord> acceptedAmountGreaterThanTen(List<PaymentRecord> acceptedRecords) {
        return acceptedRecords.stream()
                .filter(record -> record.getAmount() != null)
                .filter(record -> record.getAmount().compareTo(AMOUNT_THRESHOLD) > 0)
                .toList();
    }
}