-- EventGuard PostgreSQL schema
-- Database: eventguard_dev
-- Schema: public

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS payment_imports;

CREATE TABLE payment_imports (
                                 id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                 source_name TEXT NOT NULL,
                                 imported_at TIMESTAMP NOT NULL,
                                 parse_status TEXT NOT NULL,
                                 description TEXT NOT NULL,

                                 CONSTRAINT parse_status_allowed_check
                                     CHECK (parse_status IN ('SUCCESS', 'PARTIAL_SUCCESS', 'FAILURE'))
);

CREATE TABLE payments (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          import_id BIGINT NOT NULL,

                          payment_id TEXT NOT NULL,
                          account_id TEXT NOT NULL,
                          customer_name TEXT NOT NULL,
                          customer_email TEXT NOT NULL,

                          amount NUMERIC(19, 2),
                          currency TEXT,
                          payment_status TEXT NOT NULL,
                          rejection_status TEXT NOT NULL,

                          CONSTRAINT payments_import_fk
                              FOREIGN KEY (import_id)
                                  REFERENCES payment_imports(id),

                          CONSTRAINT amount_positive_check
                              CHECK (amount IS NULL OR amount > 0),

                          CONSTRAINT currency_length_check
                              CHECK (currency IS NULL OR char_length(currency) = 3),

                          CONSTRAINT payment_status_allowed_check
                              CHECK (payment_status IN ('PENDING', 'COMPLETED', 'FAILED')),

                          CONSTRAINT rejection_status_allowed_check
                              CHECK (rejection_status IN (
                                                          'NONE',
                                                          'MISSING_AMOUNT',
                                                          'MISSING_CURRENCY',
                                                          'DUPLICATE_PAYMENT_ID'
                                  ))
);