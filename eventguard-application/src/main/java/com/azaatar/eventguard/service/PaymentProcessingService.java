package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentProcessingReport;
import com.azaatar.eventguard.domain.PaymentRecord;
import com.azaatar.eventguard.domain.RejectedPaymentRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.azaatar.eventguard.domain.RejectionReason.DUPLICATE_PAYMENT_ID;

public class PaymentProcessingService {


    public PaymentProcessingReport process(List<PaymentRecord> records) {

        if (records == null) {
            throw new IllegalArgumentException("Records must not be null");
        }

        if (records.isEmpty()){
            return new PaymentProcessingReport(List.of(), List.of());
        }

        List<PaymentRecord> accepted = new ArrayList<>();
        List<RejectedPaymentRecord> rejected = new ArrayList<>();
        Set<String> seenPaymentIds = new HashSet<>();

        for (PaymentRecord record : records) {
            if (!seenPaymentIds.add(record.getPaymentId())) {
                RejectedPaymentRecord rejectedRecord = new RejectedPaymentRecord(record, DUPLICATE_PAYMENT_ID);
                rejected.add(rejectedRecord);
            } else {
                accepted.add(record);
            }
        }
        return new PaymentProcessingReport(accepted, rejected);
    }


}
