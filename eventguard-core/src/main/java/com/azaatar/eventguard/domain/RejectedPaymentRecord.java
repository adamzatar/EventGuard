package com.azaatar.eventguard.domain;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RejectedPaymentRecord that = (RejectedPaymentRecord) o;
        return Objects.equals(paymentRecord, that.paymentRecord) && reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentRecord, reason);
    }
}
