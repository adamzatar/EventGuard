package com.azaatar.eventguard.pojo;

import com.azaatar.eventguard.domain.PaymentRecord;

import java.util.List;
import java.util.Objects;

public class PaymentParseResult {

    private final ParseStatus parseStatus;
    private final String description;
    private final List<PaymentRecord> records;

    public PaymentParseResult(ParseStatus parseStatus, String description, List<PaymentRecord> records) {
        this.parseStatus = Objects.requireNonNull(parseStatus, "status must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.records = List.copyOf(Objects.requireNonNull(records, "records must not be null"));    }

    public boolean isSuccess() {
        return parseStatus == ParseStatus.SUCCESS || parseStatus == ParseStatus.PARTIAL_SUCCESS;
    }

    public String getDescription() {
        return description;
    }

    public List<PaymentRecord> getRecords() {
        return records;
    }

    public ParseStatus getParseStatus() {
        return parseStatus;
    }
}