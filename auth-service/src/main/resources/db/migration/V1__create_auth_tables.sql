-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTIVATION',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_roles table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(50) NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_customer_id ON users(customer_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_username ON refresh_tokens(username);

-- Insert default admin user (password: admin123, BCrypt hashed)
INSERT INTO users (username, password, customer_id, status, created_at, updated_at)
VALUES ('admin', '$2a$12$LqFhJ8zKZQvXN9VqR7nGpO8tYxW5mKjL3dRfGhI2sA4bC6eD8fE0gH', 'ADMIN001', 'ACTIVE', NOW(), NOW());

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin';

-- Insert default support user
INSERT INTO users (username, password, customer_id, status, created_at, updated_at)
VALUES ('support', '$2a$12$LqFhJ8zKZQvXN9VqR7nGpO8tYxW5mKjL3dRfGhI2sA4bC6eD8fE0gH', 'SUPPORT001', 'ACTIVE', NOW(), NOW());

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_SUPPORT' FROM users WHERE username = 'support';
