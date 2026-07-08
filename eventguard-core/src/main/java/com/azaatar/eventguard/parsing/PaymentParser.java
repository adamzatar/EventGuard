package com.azaatar.eventguard.parsing;
import com.azaatar.eventguard.domain.PaymentRecord;
import java.util.List;
// List<String> -> List<PaymentRecord>

public interface PaymentParser {
    List<PaymentRecord> parse(List<String> lines);
}

// String document is the parse parameter so that HTTP implementation works, split by comma
// two sites read and study them: quartz and pojo
// implement design in drawing
// change CSVPaymentParserTest