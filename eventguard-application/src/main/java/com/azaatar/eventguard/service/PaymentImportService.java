package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.PaymentParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Coordinates payment file reading and parsing.
 */

public class PaymentImportService {


    private final PaymentFileReader reader;
    private final PaymentParser parser;

    public PaymentImportService(PaymentFileReader reader, PaymentParser parser) {
        if (reader == null){
            throw new IllegalArgumentException("Reader can't be null!");
        }
        if (parser == null){
            throw new IllegalArgumentException("Parser can't be null!");
        }
        this.reader = reader;
        this.parser = parser;
    }

    public List<PaymentRecord> importPayments(Path path) throws IOException {
        return parser.parse(reader.readLines(path));
    }



}