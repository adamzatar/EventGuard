package com.azaatar.eventguard.ingestion;

import com.azaatar.eventguard.pojo.PaymentParseResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// Path -> List<String>

public interface PaymentFileReader {
    String read(Path path) throws IOException;
}