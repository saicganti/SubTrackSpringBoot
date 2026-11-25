CREATE TABLE categories(
    id  SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE subscriptions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    cost NUMERIC(18,2) NOT NULL CHECK (cost > 0),
    billing_cycle VARCHAR(20) NOT NULL,
    next_renewal_date TIMESTAMP NOT NULL,
    category_id INT NOT NULL REFERENCES categories(id),
    payment_method VARCHAR(50),
    notes VARCHAR(500),
    user_id VARCHAR(450) NOT NULL
);

CREATE INDEX idx_sub_user_date ON subscriptions(user_id, next_renewal_date);