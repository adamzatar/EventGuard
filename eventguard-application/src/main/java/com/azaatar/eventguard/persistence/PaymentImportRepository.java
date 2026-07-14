package com.azaatar.eventguard.persistence;

public interface PaymentImportRepository {
   long save(PaymentImportAttempt attempt);
}