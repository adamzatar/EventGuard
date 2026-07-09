package com.azaatar.eventguard.service;

import com.azaatar.eventguard.ingestion.PaymentFileReader;
import com.azaatar.eventguard.parsing.PaymentParser;
import com.azaatar.eventguard.pojo.PaymentParseResult;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Coordinates payment file reading and parsing.
 */


// add new field in the csv file to chose which output layer should be executed in order to present file
    // processing result
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

    public PaymentParseResult importPayments(Path path) throws IOException {
        String document = reader.read(path);
        return parser.parse(document);
    }



}