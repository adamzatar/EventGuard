package com.azaatar.eventguard.domain;

public class RejectedPaymentRecord {

    private final PaymentRecord paymentRecord;
    private final RejectionReason reason;

    public RejectedPaymentRecord(PaymentRecord paymentRecord, RejectionReason reason) {
        this.paymentRecord = paymentRecord;
        this.reason = reason;
    }

    public PaymentRecord getPaymentRecord() {
        return paymentRecord;
    }

    public RejectionReason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "RejectedPaymentRecord{" +
                "paymentId='" + paymentRecord.getPaymentId() + '\'' +
                ", accountId='" + paymentRecord.getAccountId() + '\'' +
                ", name='" + paymentRecord.getName() + '\'' +
                ", reason=" + reason +
                '}';
    }
}
