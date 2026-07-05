package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.azaatar.eventguard.domain.RejectionStatus.*;

public class FilePaymentReportWriter {

    private static final BigDecimal AMOUNT_THRESHOLD = new BigDecimal("10");

    public void write(PaymentProcessingReport report, Path outputPath) throws IOException {

        if (report == null) {
            throw new IllegalArgumentException("Cannot write with a null report!");
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("Cannot write with a null output path!");
        }
        validateRecords("Accepted", report.getAcceptedRecords());
        validateRecords("Rejected", report.getRejectedRecords());

        ensureParentDirectoryExists(outputPath);
        StringBuilder builder = new StringBuilder();

        appendSection(builder, "Accepted records: ", report.getAcceptedRecords());
        appendSection(builder, "Rejected Payments: Missing Currency", rejectedMissingCurrency(report));
        appendSection(builder, "Rejected Payments: Missing Amount", rejectedMissingAmount(report));
        appendSection(builder, "Other Rejected Payments", otherRejected(report));
        appendSection(builder, "Accepted Payments With Amount Greater Than 10", acceptedAmountGreaterThanTen(report));

        builder.append(System.lineSeparator());
        builder.append("Report written by Adam Zaatar.").append(System.lineSeparator());

        Files.writeString(outputPath, builder.toString());

    }
    private void validateRecords(String sectionName, List<PaymentRecord> records) {
        if (records == null) {
            throw new IllegalArgumentException(sectionName + " records must not be null");
        }

        if(records.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(sectionName + " records must not contain null records");
        }

    }
    private void ensureParentDirectoryExists(Path outputPath) throws IOException {
        Path parent = outputPath.getParent();
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

    private List<PaymentRecord> rejectedMissingCurrency(PaymentProcessingReport report) {
        return report.getRejectedRecords().stream()
                .filter(record -> record.getRejectionStatus() == MISSING_CURRENCY)
                .toList();
    }

    private List<PaymentRecord> rejectedMissingAmount(PaymentProcessingReport report) {
        return report.getRejectedRecords().stream()
                .filter(record -> record.getRejectionStatus() == MISSING_AMOUNT)
                .toList();
    }

    private List<PaymentRecord> otherRejected(PaymentProcessingReport report) {
        return report.getRejectedRecords().stream()
                .filter(record -> record.getRejectionStatus() != MISSING_CURRENCY)
                .filter(record -> record.getRejectionStatus() != MISSING_AMOUNT)
                .toList();
    }

    private List<PaymentRecord> acceptedAmountGreaterThanTen(PaymentProcessingReport report) {
        return report.getAcceptedRecords().stream()
                .filter(record -> record.getRejectionStatus() == NONE)
                .filter(record -> record.getAmount().compareTo(AMOUNT_THRESHOLD) > 0)
                .toList();
    }
}