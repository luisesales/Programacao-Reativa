CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE orders (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    total_price DECIMAL NOT NULL
);

CREATE TABLE order_items (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INT NOT NULL
);

CREATE TABLE IF NOT EXISTS saga_instance (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    
    state VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS saga_context (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    order_id UUID NOT NULL,
    total_price DOUBLE PRECISION NOT NULL,
    transaction_id UUID,    
    stock_reservation_id UUID,
    saga_id UUID NOT NULL UNIQUE,
    CONSTRAINT fk_saga_context_saga
        FOREIGN KEY (saga_id)
        REFERENCES saga_instance(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS saga_context_products_quantity (
    id UUID PRIMARY KEY,
    saga_context_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT fk_saga_ctx_product
        FOREIGN KEY (saga_context_id)
        REFERENCES saga_context(id)
        ON DELETE CASCADE
);
