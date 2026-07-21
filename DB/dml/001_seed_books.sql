INSERT INTO books (isbn, name, author, introduction)
VALUES
    ('9789865029570', 'Effective Java 中文版（第三版）', 'Joshua Bloch', '以實務案例說明 Java API、泛型、並行與物件設計。'),
    ('9789864342946', '無瑕的程式碼', 'Robert C. Martin', '介紹如何撰寫容易理解、維護與測試的程式碼。'),
    ('9789573289099', '領域驅動設計', 'Eric Evans', '以領域模型與共同語言處理複雜軟體設計。')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    author = VALUES(author),
    introduction = VALUES(introduction);

INSERT INTO inventory (id, isbn, store_time, status)
VALUES
    (101, '9789865029570', '2026-01-10 09:00:00.000000', 'AVAILABLE'),
    (102, '9789865029570', '2026-01-10 09:00:00.000000', 'AVAILABLE'),
    (103, '9789864342946', '2026-02-15 09:00:00.000000', 'AVAILABLE'),
    (104, '9789864342946', '2026-02-15 09:00:00.000000', 'PROCESSING'),
    (105, '9789573289099', '2026-03-20 09:00:00.000000', 'AVAILABLE')
ON DUPLICATE KEY UPDATE
    isbn = VALUES(isbn),
    store_time = VALUES(store_time),
    status = IF(inventory.status = 'BORROWED', inventory.status, VALUES(status));
