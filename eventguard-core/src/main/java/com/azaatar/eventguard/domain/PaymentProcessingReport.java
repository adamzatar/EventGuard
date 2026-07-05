package com.azaatar.eventguard.domain;

import java.util.List;

public class PaymentProcessingReport {

    private final List<PaymentRecord> acceptedRecords;
    private final List<PaymentRecord> rejectedPaymentRecords;

    public PaymentProcessingReport(List<PaymentRecord> acceptedRecords, List<PaymentRecord> rejectedPaymentRecords) {
        this.acceptedRecords = List.copyOf(acceptedRecords);
        this.rejectedPaymentRecords = List.copyOf(rejectedPaymentRecords);
    }

    public List<PaymentRecord> getAcceptedRecords() {
        return acceptedRecords;
    }

    public List<PaymentRecord> getRejectedRecords() {
        return rejectedPaymentRecords;
    }

    public int getTotalCount() {
        return getAcceptedCount() + getRejectedCount();
    }

    public int getAcceptedCount() {
        return acceptedRecords.size();
    }

    public int getRejectedCount() {
        return rejectedPaymentRecords.size();
    }
}
