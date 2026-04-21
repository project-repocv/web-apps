-- V1__create_customer_table.sql
-- Initial schema for Customer Service

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    customer_id UUID NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(500),
    date_of_birth DATE,
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    encrypted_ssn VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX IF NOT EXISTS idx_customers_customer_id ON customers(customer_id);
CREATE INDEX IF NOT EXISTS idx_customers_kyc_status ON customers(kyc_status);

-- Comment on table and columns
COMMENT ON TABLE customers IS 'Stores customer profile information';
COMMENT ON COLUMN customers.customer_id IS 'Unique business identifier for the customer';
COMMENT ON COLUMN customers.kyc_status IS 'KYC verification status: PENDING, VERIFIED, REJECTED';
COMMENT ON COLUMN customers.encrypted_ssn IS 'AES-256 encrypted Social Security Number';
