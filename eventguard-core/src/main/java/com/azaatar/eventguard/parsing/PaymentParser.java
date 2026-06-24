package com.azaatar.eventguard.parsing;
import com.azaatar.eventguard.domain.PaymentRecord;
import java.util.List;
// List<String> -> List<PaymentRecord>

public interface PaymentParser {
    List<PaymentRecord> parse(List<String> lines);
}