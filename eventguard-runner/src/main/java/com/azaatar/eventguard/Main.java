package com.azaatar.eventguard;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.ingestion.NioPaymentFileReader;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.CsvPaymentParser;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.reporting.ConsolePaymentReportPresenter;
import com.azaatar.eventguard.service.PaymentImportService;
import com.azaatar.eventguard.service.PaymentProcessingService;
import com.azaatar.eventguard.reporting.FilePaymentReportPresenter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            // Setup (Prepare Objects)
            PaymentFileReader reader = new NioPaymentFileReader();
            PaymentParser parser = new CsvPaymentParser();
            PaymentImportService importService = new PaymentImportService(reader, parser);
            PaymentProcessingService processingService = new PaymentProcessingService();
            Path inputPath = Path.of("eventguard-runner/src/main/resources/payments.csv");
            // Start Processing
            List<PaymentRecord> records = importService.importPayments(inputPath);
            List<PaymentRecord> processedRecords = processingService.process(records);
            // Print Output
            ConsolePaymentReportPresenter presenter = new ConsolePaymentReportPresenter();
            presenter.present(processedRecords);
            FilePaymentReportPresenter reportPresenter = new FilePaymentReportPresenter();
            reportPresenter.present(processedRecords);
        } catch (IOException e) {
            System.err.println("Failed to read or write payment file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid payment data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}