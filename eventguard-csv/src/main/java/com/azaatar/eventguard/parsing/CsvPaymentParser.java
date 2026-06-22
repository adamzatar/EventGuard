package com.azaatar.eventguard.parsing;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// String -> PaymentRecord
// CsvPaymentParser converts one simple payment CSV line into a PaymentRecord.

public class CsvPaymentParser implements PaymentParser {

    private static final int EXPECTED_COLUMN_COUNT = 7;

    /*
    1. If lines is null, throw IllegalArgumentException.
    2. Create an empty List<PaymentRecord>.
    3. Loop through each String line in lines.
    4. Convert each line into one PaymentRecord.
    5. Add each PaymentRecord to the list.
    6. Return the list.
     */
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
        /*
        If line is null, throw IllegalArgumentException.
        If line is blank, throw IllegalArgumentException.
         */

        if (line == null) {
            throw new IllegalArgumentException("Line can't be null!");
        }
        if (line.isBlank()) {
            throw new IllegalArgumentException("Line can't be blank!");
        }

        String[] columns = line.split(",", -1);

        if (columns.length != EXPECTED_COLUMN_COUNT) {
            throw new IllegalArgumentException("Line doesn't have correct number of columns");
        }

        /*
        Order:
        columns[0] -> paymentId
        columns[1] -> accountId
        columns[2] -> name
        columns[3] -> email
        columns[4] -> amount text
        columns[5] -> currency
        columns[6] -> status
         */

        String paymentId = columns[0].trim();
        String accountId = columns[1].trim();
        String name = columns[2].trim();
        String email = columns[3].trim();
        String amountText = columns[4].trim();
        String currency = columns[5].trim();
        String statusText = columns[6].trim();
        PaymentStatus status = PaymentStatus.valueOf(statusText);
        BigDecimal amount;

        if (paymentId.isBlank()
                || accountId.isBlank()
                || name.isBlank()
                || email.isBlank()
                || amountText.isBlank()
                || currency.isBlank()
                || statusText.isBlank()) {
            throw new IllegalArgumentException("Required payment field must not be blank");
        }

        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Amount must be a valid decimal for BigDecimal!");
        }


        return new PaymentRecord(paymentId, accountId, name, email, amount, currency, status);

    }

    public static int getExpectedColumnCount() {
        return EXPECTED_COLUMN_COUNT;
    }
}