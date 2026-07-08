package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.RejectionStatus;

import java.util.List;

public class ConsolePaymentReportPresenter implements PaymentReportPresenter {


    public void present(List<PaymentRecord> processedRecords) {

        StringBuilder builder = new StringBuilder();
        List<PaymentRecord> acceptedRecords = processedRecords.stream()
                .filter(record -> record.getRejectionStatus() == RejectionStatus.NONE)
                .toList();
        List<PaymentRecord> rejectedRecords = processedRecords.stream()
                .filter(record -> record.getRejectionStatus() != RejectionStatus.NONE)
                .toList();

        builder.append("Payment processing report\n");
        builder.append("=========================\n");
        builder.append("Total records: ").append(processedRecords.size()).append("\n");
        builder.append("Accepted records: ").append(acceptedRecords.size()).append("\n");
        builder.append("Rejected records: ").append(rejectedRecords.size()).append("\n\n");

        builder.append("Accepted payments\n");
        builder.append("-----------------\n");
        for (PaymentRecord record : acceptedRecords) {
            builder.append(formatAcceptedRecords(record)).append("\n\n");
        }

        builder.append("Rejected payments\n");
        builder.append("-----------------\n");
        for (PaymentRecord rejectedRecord : rejectedRecords) {
            builder.append(formatRejectedRecords(rejectedRecord)).append("\n\n");
        }

        System.out.println("Console Write *****************************************");
        System.out.println(builder);
    }

    private String formatRejectedRecords(PaymentRecord record) {

        return "Rejected payment record:\n" +
                "Payment ID: " + record.getPaymentId() + "\n" +
                "Account ID: " + record.getAccountId() + "\n" +
                "Customer: " + record.getName() + "\n" +
                "Email: " + record.getEmail() + "\n" +
                "Amount: " + record.getAmount() + " " + record.getCurrency() + "\n" +
                "Status: " + record.getStatus() + "\n" +
                "Rejection reason: " + record.getRejectionStatus();
    }

    private String formatAcceptedRecords(PaymentRecord record) {
        return "Payment record:\n" +
                "Payment ID: " + record.getPaymentId() + "\n" +
                "Account ID: " + record.getAccountId() + "\n" +
                "Customer: " + record.getName() + "\n" +
                "Email: " + record.getEmail() + "\n" +
                "Amount: " + record.getAmount() + " " + record.getCurrency() + "\n" +
                "Status: " + record.getStatus();
    }
}
