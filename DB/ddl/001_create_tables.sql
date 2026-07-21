CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    registration_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_login_time DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number),
    CONSTRAINT ck_users_phone_number
        CHECK (phone_number REGEXP '^09[0-9]{8}$'),
    CONSTRAINT ck_users_user_name
        CHECK (CHAR_LENGTH(TRIM(user_name)) BETWEEN 1 AND 100)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS books (
    isbn VARCHAR(13) NOT NULL,
    name VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    introduction TEXT NULL,
    PRIMARY KEY (isbn),
    CONSTRAINT ck_books_isbn
        CHECK (isbn REGEXP '^([0-9]{10}|[0-9]{13})$'),
    CONSTRAINT ck_books_name
        CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_books_author
        CHECK (CHAR_LENGTH(TRIM(author)) > 0)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    isbn VARCHAR(13) NOT NULL,
    store_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    PRIMARY KEY (id),
    KEY idx_inventory_isbn_status (isbn, status),
    CONSTRAINT fk_inventory_book
        FOREIGN KEY (isbn) REFERENCES books (isbn)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_inventory_status
        CHECK (status IN (
            'AVAILABLE',
            'BORROWED',
            'PROCESSING',
            'LOST',
            'DAMAGED',
            'DISCARDED'
        ))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS borrowing_records (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    inventory_id BIGINT UNSIGNED NOT NULL,
    borrowing_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    return_time DATETIME(6) NULL,
    active_inventory_id BIGINT UNSIGNED
        GENERATED ALWAYS AS (
            CASE
                WHEN return_time IS NULL THEN inventory_id
                ELSE NULL
            END
        ) STORED,
    PRIMARY KEY (id),
    KEY idx_borrowing_records_user_time (user_id, borrowing_time DESC),
    KEY idx_borrowing_records_inventory_time (inventory_id, borrowing_time DESC),
    CONSTRAINT uk_borrowing_records_active_inventory UNIQUE (active_inventory_id),
    CONSTRAINT fk_borrowing_records_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_borrowing_records_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventory (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT ck_borrowing_records_return_time
        CHECK (return_time IS NULL OR return_time >= borrowing_time)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
