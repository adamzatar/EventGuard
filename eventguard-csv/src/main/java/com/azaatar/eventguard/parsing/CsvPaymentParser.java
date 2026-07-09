package com.azaatar.eventguard.parsing;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;
import com.azaatar.eventguard.domain.RejectionStatus;
import com.azaatar.eventguard.pojo.ParseStatus;
import com.azaatar.eventguard.pojo.PaymentParseResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// CsvPaymentParser converts a payment CSV document into a PaymentParseResult.

public class CsvPaymentParser implements PaymentParser {

    private static final int EXPECTED_COLUMN_COUNT = 7;

    @Override
    public PaymentParseResult parse(String document) {

        if (document == null) {
            throw new IllegalArgumentException("Document must not be null!");
        }

        if (document.isBlank()) {
            return new PaymentParseResult(ParseStatus.FAILURE, "Document parsing failed: Document must not be blank.", List.of());
        }

        List<String> lines = document.lines().toList();
        List<PaymentRecord> records = new ArrayList<>();


        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            try {
                records.add(parseLine(line));
            } catch (IllegalArgumentException exception) {
                return new PaymentParseResult(ParseStatus.FAILURE, buildFailureDescription(i + 1, exception.getMessage()), List.of());
            }
        }

        ParseStatus parseStatus = determineParseStatus(records);
        String description = buildDescription(parseStatus, records);

        return new PaymentParseResult(parseStatus, description, records);
    }

    private PaymentRecord parseLine(String line) {

        validateLine(line);

        String[] columns = line.split(",", -1);
        validateColumnCount(columns);

        List<String> fields = new ArrayList<>();

        for (int i = 0; i < EXPECTED_COLUMN_COUNT; i++) {
            String field = columns[i].trim();
            fields.add(field);
        }

        String paymentId = fields.get(0);
        String accountId = fields.get(1);
        String name = fields.get(2);
        String email = fields.get(3);
        String amountText = fields.get(4);
        String currency = fields.get(5);
        String statusText = fields.get(6);

        validateRequiredFields(paymentId, "Payment ID");
        validateRequiredFields(accountId, "Account ID");
        validateRequiredFields(name, "Name");
        validateRequiredFields(email, "Email");
        validateRequiredFields(statusText, "Status");

        RejectionStatus rejectionStatus = RejectionStatus.NONE;

        BigDecimal amount;
        if (amountText.isBlank()) {
            amount = null;
            rejectionStatus = RejectionStatus.MISSING_AMOUNT;
        } else {
            amount = parseAmount(amountText);
        }

        // give priority for amount in RejectionStatus
        if (currency.isBlank() && rejectionStatus == RejectionStatus.NONE) {
            rejectionStatus = RejectionStatus.MISSING_CURRENCY;
        }

        PaymentStatus status = parsePaymentStatus(statusText);

        PaymentRecord record = new PaymentRecord(paymentId, accountId, name, email, amount, currency, status);
        record.setRejectionStatus(rejectionStatus);

        return record;
    }

    /*
    missing amount
    missing currency
    invalid amount format
    invalid status text
    wrong column count
    blank required raw field
     */
    private void validateRequiredFields(String value, String fieldName) {
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

    private PaymentStatus parsePaymentStatus(String statusText) {
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

    private ParseStatus determineParseStatus(List<PaymentRecord> records) {
        boolean hasParserRejectedRecord = records.stream().anyMatch(record -> record.getRejectionStatus() == RejectionStatus.MISSING_AMOUNT || record.getRejectionStatus() == RejectionStatus.MISSING_CURRENCY);

        return hasParserRejectedRecord ? ParseStatus.PARTIAL_SUCCESS : ParseStatus.SUCCESS;
    }

    private String buildDescription(ParseStatus parseStatus, List<PaymentRecord> records) {
        int totalRecords = records.size();

        long missingAmountCount = records.stream().filter(record -> record.getRejectionStatus() == RejectionStatus.MISSING_AMOUNT).count();

        long missingCurrencyCount = records.stream().filter(record -> record.getRejectionStatus() == RejectionStatus.MISSING_CURRENCY).count();

        long parserRejectedCount = missingAmountCount + missingCurrencyCount;
        long validCount = totalRecords - parserRejectedCount;

        if (parseStatus == ParseStatus.SUCCESS) {
            return "Document parsed successfully. Parsed " + totalRecords + " record(s) with no parser-level rejections.";
        }

        if (parseStatus == ParseStatus.PARTIAL_SUCCESS) {
            return "Document parsed with parser-level rejected row(s). Parsed " + totalRecords + " record(s): " + validCount + " valid, " + missingAmountCount + " missing amount, " + missingCurrencyCount + " missing currency.";
        }

        return "Document parsing failed.";
    }

    private String buildFailureDescription(int lineNumber, String reason) {
        return "Document parsing failed at line " + lineNumber + ": " + reason;
    }

}