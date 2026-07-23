package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.persistence.PaymentImportAttempt;
import com.azaatar.eventguard.persistence.PaymentImportRepository;
import com.azaatar.eventguard.pojo.PaymentParseResult;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PaymentImportWorkflowService {

    private final PaymentImportService importService;
    private final PaymentProcessingService processingService;
    private final PaymentImportRepository paymentImportRepository;

    public PaymentImportWorkflowService(
            PaymentImportService importService,
            PaymentProcessingService processingService,
            PaymentImportRepository paymentImportRepository
    ) {
        this.importService = Objects.requireNonNull(importService, "importService must not be null");
        this.processingService = Objects.requireNonNull(processingService, "processingService must not be null");
        this.paymentImportRepository = Objects.requireNonNull(paymentImportRepository, "paymentImportRepository must not be null");
    }

    public long importPayments(Path sourcePath) throws IOException {
        Objects.requireNonNull(sourcePath, "sourcePath must not be null");

        PaymentParseResult parseResult = importService.importPayments(sourcePath);

        List<PaymentRecord> finalRecords;
        if (parseResult.isSuccess()) {
            finalRecords = processingService.process(parseResult.getRecords());
        } else {
            finalRecords = List.of();
        }

        PaymentImportAttempt paymentImportAttempt = createPaymentImportAttempt(sourcePath, parseResult, finalRecords);

        return paymentImportRepository.save(paymentImportAttempt);
    }

    private PaymentImportAttempt createPaymentImportAttempt(
            Path sourcePath,
            PaymentParseResult parseResult,
            List<PaymentRecord> finalRecords
    ) {
        String sourceName = sourcePath.getFileName().toString();

        return new PaymentImportAttempt(
                sourceName,
                LocalDateTime.now(),
                parseResult.getParseStatus(),
                parseResult.getDescription(),
                finalRecords
        );
    }
}