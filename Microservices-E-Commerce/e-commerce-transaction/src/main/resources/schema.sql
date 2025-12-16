CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS transactions (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_price DECIMAL NOT NULL,
    order_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP NULL,
    last_attempt_at TIMESTAMP NULL,
    last_error VARCHAR(500) NULL,
    retry_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS outbox_event_context (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    outbox_event_id UUID NOT NULL REFERENCES outbox_event(id) ON DELETE CASCADE,
    field_name VARCHAR(255) NOT NULL,
    field_value VARCHAR(255) NOT NULL,
    transaction_id UUID NOT NULL REFERENCES transactions(id) ON DELETE CASCADE
);