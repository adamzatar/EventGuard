package com.azaatar.eventguard.ingestion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class NioPaymentFileReader implements PaymentFileReader {

    @Override
    public String read(Path path) throws IOException {
        return Files.readString(path);
    }
}