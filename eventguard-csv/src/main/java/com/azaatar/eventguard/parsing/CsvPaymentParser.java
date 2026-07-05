package com.azaatar.eventguard.parsing;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// CsvPaymentParser converts one simple payment CSV line into a PaymentRecord.

public class CsvPaymentParser implements PaymentParser {

    private static final int EXPECTED_COLUMN_COUNT = 7;
    private static final List<String> FIELD_NAMES = List.of(
            "Payment ID",
            "Account ID",
            "Name",
            "Email",
            "Amount",
            "Currency",
            "Status"
    );

    @Override
    public List<PaymentRecord> parse(List<String> lines) {
        if (lines == null) {
            throw new IllegalArgumentException("Lines must not be null!");
        }
        List<PaymentRecord> records = new ArrayList<>();

        for (String line : lines) {
            records.add(parseLine(line));
        }

        return records;
    }

    private PaymentRecord parseLine(String line) {

        validateLine(line);

        String[] columns = line.split(",", -1);
        validateColumnCount(columns);

        List<String> fields = new ArrayList<>();

        for (int i = 0; i < EXPECTED_COLUMN_COUNT; i++) {
            String field = columns[i].trim();
            validateField(field, FIELD_NAMES.get(i));
            fields.add(field);
        }

        String paymentId = fields.get(0);
        String accountId = fields.get(1);
        String name = fields.get(2);
        String email = fields.get(3);
        String amountText = fields.get(4);
        String currency = fields.get(5);
        String statusText = fields.get(6);


        BigDecimal amount = parseAmount(amountText);
        PaymentStatus status = parseStatus(statusText);


        return new PaymentRecord(paymentId, accountId, name, email, amount, currency, status);

    }

    private void validateField(String value, String fieldName) {
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank!");
        }
    }

    private void validateLine(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Line can't be null!");
        }
        if (line.isBlank()) {
            throw new IllegalArgumentException("Line can't be blank!");
        }
    }

    private void validateColumnCount(String[] columns) {
        if (columns.length != EXPECTED_COLUMN_COUNT) {
            throw new IllegalArgumentException("Line doesn't have correct number of columns");
        }
    }

    private PaymentStatus parseStatus(String statusText) {
        try {
            return PaymentStatus.valueOf(statusText);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be valid!");
        }
    }

    private BigDecimal parseAmount(String amountText) {
        try {
            return new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a valid decimal for BigDecimal!");
        }
    }

}