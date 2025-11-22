CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS product (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(500),
    price DECIMAL,
    stock_quantity INT,
    category VARCHAR(255)
)