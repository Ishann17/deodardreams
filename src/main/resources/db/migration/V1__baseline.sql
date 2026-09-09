CREATE TABLE room_products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6),
    created_by BIGINT,
    updated_at DATETIME(6),
    updated_by BIGINT,
    base_occupancy INT,
    base_price DECIMAL(38,2),
    extra_guest_charge DECIMAL(38,2),
    max_occupancy INT,
    name VARCHAR(255),
    room_category ENUM('ONE_BHK', 'SINGLE_SUITE', 'THREE_BHK', 'TWO_BHK'),
    PRIMARY KEY (id)
);

INSERT INTO room_products
    (id, created_at, created_by, updated_at, updated_by,
     base_occupancy, base_price, extra_guest_charge, max_occupancy, name, room_category)
VALUES
    (1, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     2, 1999.00, 500.00, 3, 'Single Room', 'SINGLE_SUITE'),

    (2, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     2, 2999.00, 0.00, 3, '1 BHK', 'ONE_BHK'),

    (3, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     4, 4999.00, 0.00, 6, '2 BHK', 'TWO_BHK'),

    (4, CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP, NULL,
     6, 7999.00, 0.00, 8, '3 BHK', 'THREE_BHK');