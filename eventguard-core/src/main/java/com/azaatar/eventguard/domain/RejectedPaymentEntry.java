package com.azaatar.eventguard.domain;

import java.util.Objects;

/*
 * RejectedPaymentEntry represents
 * one rejected-payment audit row that
 * EventGuard may persist into PostgreSQL.
 */
public class RejectedPaymentEntry {
    private final String rawLine;
    private final String paymentId;
    private final String accountId;
    private final String customerName;
    private final String customerEmail;
    private final String amountText;
    private final String currency;
    private final String statusText;
    private final RejectionReason rejectionReason;

    public RejectedPaymentEntry(String rawLine, String paymentId, String accountId, String customerName, String customerEmail, String amountText, String currency, String statusText, RejectionReason rejectionReason) {
        if (rawLine == null) {
            throw new IllegalArgumentException("Cannot pass a raw processed line as null, what are we processing then?");
        }
        if (rawLine.isBlank()) {
            throw new IllegalArgumentException("Cannot pass a blank raw processed line, what are we processing then?");
        }
        if (rejectionReason == null) {
            throw new IllegalArgumentException("There must be a reason for rejection!");
        }
        this.rawLine = rawLine;
        this.paymentId = paymentId;
        this.accountId = accountId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.amountText = amountText;
        this.currency = currency;
        this.statusText = statusText;
        this.rejectionReason = rejectionReason;
    }

    public String getRawLine() {
        return rawLine;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getAmountText() {
        return amountText;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatusText() {
        return statusText;
    }

    public RejectionReason getRejectionReason() {
        return rejectionReason;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RejectedPaymentEntry that = (RejectedPaymentEntry) o;
        return Objects.equals(rawLine, that.rawLine) && Objects.equals(paymentId, that.paymentId) && Objects.equals(accountId, that.accountId) && Objects.equals(customerName, that.customerName) && Objects.equals(customerEmail, that.customerEmail) && Objects.equals(amountText, that.amountText) && Objects.equals(currency, that.currency) && Objects.equals(statusText, that.statusText) && rejectionReason == that.rejectionReason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawLine, paymentId, accountId, customerName, customerEmail, amountText, currency, statusText, rejectionReason);
    }

    @Override
    public String toString() {
        return "RejectedPaymentEntry{" + "rawLine='" + rawLine + '\'' + ", paymentId='" + paymentId + '\'' + ", accountId='" + accountId + '\'' + ", customerName='" + customerName + '\'' + ", customerEmail='" + customerEmail + '\'' + ", amountText='" + amountText + '\'' + ", currency='" + currency + '\'' + ", statusText='" + statusText + '\'' + ", rejectionReason=" + rejectionReason + '}';
    }
}