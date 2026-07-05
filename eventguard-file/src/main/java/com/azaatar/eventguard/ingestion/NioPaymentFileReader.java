package com.azaatar.eventguard.ingestion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class NioPaymentFileReader implements PaymentFileReader {

    @Override
    public List<String> readLines(Path path) throws IOException {
        return Files.readAllLines(path);
    }
}