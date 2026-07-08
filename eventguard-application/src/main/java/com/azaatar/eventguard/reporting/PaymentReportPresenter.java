package com.azaatar.eventguard.reporting;

import com.azaatar.eventguard.domain.PaymentRecord;

import java.io.IOException;
import java.util.List;

public interface PaymentReportPresenter {
    void present(List<PaymentRecord> processedRecords) throws IOException;
}