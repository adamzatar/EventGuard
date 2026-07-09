package com.azaatar.eventguard;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.ingestion.NioPaymentFileReader;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.CsvPaymentParser;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.pojo.PaymentParseResult;
import com.azaatar.eventguard.reporting.ConsolePaymentReportPresenter;
import com.azaatar.eventguard.reporting.FilePaymentReportPresenter;
import com.azaatar.eventguard.service.PaymentImportService;
import com.azaatar.eventguard.service.PaymentProcessingService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            // Setup
            PaymentFileReader reader = new NioPaymentFileReader();
            PaymentParser parser = new CsvPaymentParser();
            PaymentImportService importService = new PaymentImportService(reader, parser);
            PaymentProcessingService processingService = new PaymentProcessingService();

            ConsolePaymentReportPresenter consolePresenter = new ConsolePaymentReportPresenter();
            FilePaymentReportPresenter filePresenter = new FilePaymentReportPresenter();

            Path inputPath = Path.of("eventguard-runner/src/main/resources/payments.csv");

            // Import / parse
            PaymentParseResult parseResult = importService.importPayments(inputPath);

            if (!parseResult.canProceedToProcessing()) {
                System.err.println(parseResult.getDescription());
                return;
            }

            // Process
            List<PaymentRecord> processedRecords = processingService.process(parseResult.getRecords());

            // Present
            consolePresenter.present(processedRecords);
            filePresenter.present(processedRecords);

        } catch (IOException e) {
            System.err.println("Failed to read or write payment file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid payment data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}