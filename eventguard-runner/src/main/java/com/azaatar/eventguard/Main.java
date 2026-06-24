package com.azaatar.eventguard;

import com.azaatar.eventguard.ingestion.NioPaymentFileReader;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.CsvPaymentParser;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.service.PaymentImportService;

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
            // create service
            PaymentImportService service = new PaymentImportService(reader, parser);
            // choose path
            Path path = Path.of("eventguard-runner/src/main/resources/payments.csv");
            // print records
            List<PaymentRecord> records = service.importPayments(path);
            for(PaymentRecord record : records){
                System.out.println(record);
            }
        } catch (IOException e) {
            System.err.println(STR."Failed to import payments: \{e.getMessage()}");
        }
    }
}
