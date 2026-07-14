-- EventGuard PostgreSQL schema
-- Database: eventguard_dev
-- Schema: public

DROP TABLE IF EXISTS payments;

-- foreign key lal file yishbok 3al payments
CREATE TABLE payments (
                          id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                          payment_id TEXT NOT NULL UNIQUE,
                          account_id TEXT NOT NULL,
                          customer_name TEXT NOT NULL,
                          customer_email TEXT NOT NULL,

                          amount NUMERIC(19, 2) NOT NULL,
                          currency TEXT NOT NULL,
                          status TEXT NOT NULL,

                          CONSTRAINT amount_positive_check CHECK (amount > 0),
                          CONSTRAINT currency_length_check CHECK (char_length(currency) = 3),
                          CONSTRAINT status_allowed_check CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);
