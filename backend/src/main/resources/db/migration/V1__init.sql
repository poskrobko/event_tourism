CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    map_url VARCHAR(512)
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    event_date DATE NOT NULL,
    base_price NUMERIC(12,2) NOT NULL,
    location_id BIGINT NOT NULL REFERENCES locations(id)
);

CREATE TABLE event_programs (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    activity VARCHAR(255) NOT NULL
);

CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    amount INTEGER NOT NULL,
    total_price NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP NOT NULL
);

INSERT INTO roles(name) VALUES ('USER'), ('ADMIN');
