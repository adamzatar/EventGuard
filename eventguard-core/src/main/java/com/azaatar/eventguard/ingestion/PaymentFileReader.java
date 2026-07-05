package com.azaatar.eventguard.ingestion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// Path -> List<String>

public interface PaymentFileReader {
    List<String> readLines(Path path) throws IOException;
}