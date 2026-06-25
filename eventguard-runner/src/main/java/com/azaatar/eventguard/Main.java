package com.azaatar.eventguard;

import com.azaatar.eventguard.ingestion.NioPaymentFileReader;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.CsvPaymentParser;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.service.PaymentImportService;
import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.reporting.PaymentRecordFormatter;
import com.azaatar.eventguard.service.PaymentProcessingService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            // create reader
            PaymentFileReader reader = new NioPaymentFileReader();
            // create parser
            PaymentParser parser = new CsvPaymentParser();
            // create importService and processingService
            PaymentImportService importService = new PaymentImportService(reader, parser);
            PaymentProcessingService processingService = new PaymentProcessingService();
            PaymentRecordFormatter formatter = new PaymentRecordFormatter();
            // choose path
            Path path = Path.of("eventguard-runner/src/main/resources/payments.csv");
            // print records
            List<PaymentRecord> records = importService.importPayments(path);
            PaymentProcessingReport report = processingService.process(records);
            System.out.println(formatter.format(report));
        }  catch (IOException e) {
        System.err.println("Failed to read payment file: " + e.getMessage() );
    } catch (IllegalArgumentException e) {
        System.err.println("Invalid payment data: " + e.getMessage() );
    } catch (Exception e) {
        System.err.println("Unexpected error: " + e.getMessage());
    }


    }

}
