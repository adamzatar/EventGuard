package com.azaatar.eventguard;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.ingestion.NioPaymentFileReader;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.CsvPaymentParser;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.reporting.FilePaymentReportWriter;
import com.azaatar.eventguard.reporting.PaymentRecordFormatter;
import com.azaatar.eventguard.service.PaymentImportService;
import com.azaatar.eventguard.service.PaymentProcessingService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            PaymentFileReader reader = new NioPaymentFileReader();
            PaymentParser parser = new CsvPaymentParser();

            PaymentImportService importService = new PaymentImportService(reader, parser);
            PaymentProcessingService processingService = new PaymentProcessingService();
            FilePaymentReportWriter reportWriter = new FilePaymentReportWriter();

            Path inputPath = Path.of("eventguard-runner/src/main/resources/payments.csv");
            Path outputPath = Path.of("reports/payment-processing-report.txt");

            List<PaymentRecord> records = importService.importPayments(inputPath);
            PaymentProcessingReport report = processingService.process(records);

            Path formatterOutputPath = Path.of("/tmp/eventguard-formatter-output.txt");
            Path writerOutputPath = Path.of("reports/payment-processing-report.txt");

            PaymentRecordFormatter formatter = new PaymentRecordFormatter();
            Files.writeString(formatterOutputPath, formatter.format(report));

            reportWriter.write(report, writerOutputPath);

            System.out.println("Formatter output written to: " + formatterOutputPath);
            System.out.println("Writer output written to: " + writerOutputPath.toAbsolutePath());

            System.out.println("Payment report written to: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to read or write payment file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid payment data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}