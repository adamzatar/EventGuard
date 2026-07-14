package com.azaatar.eventguard.persistence;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.pojo.ParseStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PaymentImportAttempt {
    private final String sourceName;
    private final LocalDateTime importedAt;
    private final ParseStatus parseStatus;
    private final String description;
    private final List<PaymentRecord> records;


    public PaymentImportAttempt(String sourceName, LocalDateTime importedAt, ParseStatus parseStatus, String description, List<PaymentRecord> records) {
        validateNotBlank(sourceName, "sourceName");
        validateNotBlank(description, "description");
        this.sourceName = sourceName.trim();
        this.importedAt = Objects.requireNonNull(importedAt, "importedAt must not be null!");
        this.parseStatus = Objects.requireNonNull(parseStatus, "parseStatus must not be null!");
        this.description = description.trim();
        this.records = validateAndCopyRecords(this.parseStatus, records);
    }

    private static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank!");
        }
    }

    private static List<PaymentRecord> validateAndCopyRecords(ParseStatus parseStatus, List<PaymentRecord> records) {
        List<PaymentRecord> copiedRecords = List.copyOf(Objects.requireNonNull(records, "records must not be null"));
        if (parseStatus == ParseStatus.FAILURE && !copiedRecords.isEmpty()) {
            throw new IllegalArgumentException("failed import attempts must not contain payment records");
        }
        return copiedRecords;
    }

    public String getSourceName() {
        return sourceName;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public ParseStatus getParseStatus() {
        return parseStatus;
    }

    public String getDescription() {
        return description;
    }

    public List<PaymentRecord> getRecords() {
        return records;
    }
}