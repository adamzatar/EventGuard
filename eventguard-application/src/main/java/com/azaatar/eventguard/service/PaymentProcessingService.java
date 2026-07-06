package com.azaatar.eventguard.service;

import com.azaatar.eventguard.domain.PaymentRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.azaatar.eventguard.domain.RejectionStatus.DUPLICATE_PAYMENT_ID;
import static com.azaatar.eventguard.domain.RejectionStatus.NONE;

public class PaymentProcessingService {


    // no filteration where processing services are, in formatter
    // logic of filteration is on service layer, both processing service and processing report will expand way too much

    // new implementation, services should process and label data not
    // keep the code

    // isolate functionality between layers

    // return data as a report in two places on the terminal and on file, the file has different formatting
    // one file formatter, one terminal formatter
    // if I made a new implementation for terminal, we'll have to write on a file because we are making the class carry too many responsibilities
    // Single responsibility inject your object whereever you want
    // dont change anything on code before the TODO

    // Do this first and foremost
    // TODO:
    // Create a new formatter that will write the result, that will create a new file and write all the result into the file (new class)
    // write the accepted, rejected (error missing currency), rejected (missing amount), then all rejected without missing currency nor missing amount and print all accepted whose amount > 10.
    //
    // file at the bottom, add copyright that file is written by me (footer).

    public List<PaymentRecord> process(List<PaymentRecord> processedRecords) {

            if (processedRecords == null) {
                throw new IllegalArgumentException("Records must not be null");
            }

            if (processedRecords.isEmpty()) {
                return List.of();
            }

            Set<String> seenPaymentIds = new HashSet<>();

            for (PaymentRecord record : processedRecords) {

                if (record.getRejectionStatus() != NONE) {
                    continue;
                }
                if (!seenPaymentIds.add(record.getPaymentId())) {
                    record.setRejectionStatus(DUPLICATE_PAYMENT_ID);
            }

        }
        return processedRecords;

    }


}
