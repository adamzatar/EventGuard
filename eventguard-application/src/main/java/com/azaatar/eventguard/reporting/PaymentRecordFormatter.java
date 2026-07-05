package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;

public class PaymentRecordFormatter {

    public String format(PaymentRecord record) {
        return "Payment record:\n" +
                "Payment ID: " + record.getPaymentId() + "\n" +
                "Account ID: " + record.getAccountId() + "\n" +
                "Customer: " + record.getName() + "\n" +
                "Email: " + record.getEmail() + "\n" +
                "Amount: " + record.getAmount() + " " + record.getCurrency() + "\n" +
                "Status: " + record.getStatus();
    }

    public String formatRejectedRecords(PaymentRecord record) {

        return "Rejected payment record:\n" +
                "Payment ID: " + record.getPaymentId() + "\n" +
                "Account ID: " + record.getAccountId() + "\n" +
                "Customer: " + record.getName() + "\n" +
                "Email: " + record.getEmail() + "\n" +
                "Amount: " + record.getAmount() + " " + record.getCurrency() + "\n" +
                "Status: " + record.getStatus() + "\n" +
                "Rejection reason: " + record.getRejectionStatus();
    }

    public String format(PaymentProcessingReport report) {
        StringBuilder builder = new StringBuilder();

        builder.append("Payment processing report\n");
        builder.append("=========================\n");
        builder.append("Total records: ").append(report.getTotalCount()).append("\n");
        builder.append("Accepted records: ").append(report.getAcceptedCount()).append("\n");
        builder.append("Rejected records: ").append(report.getRejectedCount()).append("\n\n");

        builder.append("Accepted payments\n");
        builder.append("-----------------\n");
        for (PaymentRecord record : report.getAcceptedRecords()) {
            builder.append(format(record)).append("\n\n");
        }

        builder.append("Rejected payments\n");
        builder.append("-----------------\n");
        for (PaymentRecord rejectedRecord : report.getRejectedRecords()) {
            builder.append(format(rejectedRecord)).append("\n\n");
        }

        return builder.toString();
    }
}
