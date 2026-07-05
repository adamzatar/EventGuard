package com.azaatar.eventguard.domain;

import java.math.BigDecimal;
import java.util.Objects;


// One PaymentRecord object is a domain object that translates to a single line read from the payments CSV

public class PaymentRecord {


    private String paymentId;


    private String accountId;
    private String name;
    private String email;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private RejectionStatus rejectionStatus;

    public PaymentRecord(String paymentId, String accountId, String name, String email, BigDecimal amount, String currency, PaymentStatus status) {
        this.paymentId = paymentId;
        this.status = status;
        this.currency = currency;
        this.amount = amount;
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.rejectionStatus = RejectionStatus.NONE;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public RejectionStatus getRejectionStatus() {
        return rejectionStatus;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setRejectionStatus(RejectionStatus rejectionStatus) {
        if (rejectionStatus == null) {
            throw new IllegalArgumentException();
        }
        this.rejectionStatus = rejectionStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PaymentRecord that = (PaymentRecord) o;
        return Objects.equals(paymentId, that.paymentId) && Objects.equals(accountId, that.accountId) && Objects.equals(name, that.name) && Objects.equals(email, that.email) && Objects.equals(amount, that.amount) && Objects.equals(currency, that.currency) && status == that.status && rejectionStatus == that.rejectionStatus;
    }

    @Override
    public String toString() {
        return "PaymentRecord{" +
                "paymentId='" + paymentId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", customerName='" + name + '\'' +
                ", customerEmail='" + email + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", rejectionStatus=" + rejectionStatus +
                '}';
    }
    @Override
    public int hashCode() {
        return Objects.hash(paymentId, accountId, name, email, amount, currency, status, rejectionStatus);
    }

}
