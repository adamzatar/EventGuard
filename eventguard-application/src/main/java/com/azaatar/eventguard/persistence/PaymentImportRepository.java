package com.azaatar.eventguard.persistence;

import java.sql.SQLException;

public interface PaymentImportRepository {
   long save(PaymentImportAttempt attempt);
}