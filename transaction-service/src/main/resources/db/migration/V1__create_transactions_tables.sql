-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    source_account_number VARCHAR(20) NOT NULL,
    destination_account_number VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description TEXT,
    reference_number VARCHAR(50),
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason TEXT
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_transactions_transaction_id ON transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transactions_source_account ON transactions(source_account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_destination_account ON transactions(destination_account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_idempotency_key ON transactions(idempotency_key);

-- Create transaction_sagas table
CREATE TABLE IF NOT EXISTS transaction_sagas (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(50) UNIQUE NOT NULL,
    transaction_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_step INTEGER NOT NULL DEFAULT 0,
    compensation_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transaction_sagas_saga_id ON transaction_sagas(saga_id);
CREATE INDEX IF NOT EXISTS idx_transaction_sagas_transaction_id ON transaction_sagas(transaction_id);

-- Create idempotency_keys table
CREATE TABLE IF NOT EXISTS idempotency_keys (
    key_value VARCHAR(255) PRIMARY KEY,
    request_hash VARCHAR(255) NOT NULL,
    response_body TEXT,
    status_code INTEGER,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_idempotency_keys_expires_at ON idempotency_keys(expires_at);
