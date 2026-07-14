package com.azaatar.eventguard.parsing;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.pojo.PaymentParseResult;

import java.util.List;
// List<String> -> List<PaymentRecord>

public interface PaymentParser {
    PaymentParseResult parse(String document);
}

// String document is the parse parameter so that HTTP implementation works, split by comma
// two sites read and study them: quartz and pojo
// implement design in drawing
// change CSVPaymentParserTest