package com.azaatar.eventguard.parsing;
import com.azaatar.eventguard.domain.PaymentRecord;
import java.util.List;
// List<String> -> List<PaymentRecord>

interface PaymentParser {
    public List<PaymentRecord> parse(List<String> lines);
}