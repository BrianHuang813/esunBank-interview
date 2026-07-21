DELIMITER $$

DROP PROCEDURE IF EXISTS sp_book_list$$
CREATE PROCEDURE sp_book_list(
    IN p_keyword VARCHAR(255),
    IN p_offset INT UNSIGNED,
    IN p_limit INT UNSIGNED
)
BEGIN
    DECLARE v_keyword VARCHAR(255) DEFAULT TRIM(COALESCE(p_keyword, ''));
    DECLARE v_offset INT UNSIGNED DEFAULT COALESCE(p_offset, 0);
    DECLARE v_limit INT UNSIGNED DEFAULT COALESCE(p_limit, 20);

    IF v_limit < 1 OR v_limit > 100 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30101,
                MESSAGE_TEXT = 'INVALID_PAGE_SIZE';
    END IF;

    WITH book_summary AS (
        SELECT
            b.isbn,
            b.name,
            b.author,
            b.introduction,
            COUNT(i.id) AS inventory_count,
            COALESCE(SUM(i.status = 'AVAILABLE'), 0) AS available_count
        FROM books b
        LEFT JOIN inventory i ON i.isbn = b.isbn
        WHERE v_keyword = ''
           OR b.isbn LIKE CONCAT('%', v_keyword, '%')
           OR b.name LIKE CONCAT('%', v_keyword, '%')
           OR b.author LIKE CONCAT('%', v_keyword, '%')
        GROUP BY b.isbn, b.name, b.author, b.introduction
    )
    SELECT
        isbn,
        name,
        author,
        introduction,
        inventory_count,
        available_count,
        COUNT(*) OVER () AS total_items
    FROM book_summary
    ORDER BY name, isbn
    LIMIT v_limit OFFSET v_offset;
END$$

DROP PROCEDURE IF EXISTS sp_book_get$$
CREATE PROCEDURE sp_book_get(
    IN p_isbn VARCHAR(13)
)
BEGIN
    SELECT
        b.isbn,
        b.name,
        b.author,
        b.introduction,
        COUNT(i.id) AS inventory_count,
        COALESCE(SUM(i.status = 'AVAILABLE'), 0) AS available_count
    FROM books b
    LEFT JOIN inventory i ON i.isbn = b.isbn
    WHERE b.isbn = p_isbn
    GROUP BY b.isbn, b.name, b.author, b.introduction;

    SELECT
        id AS inventory_id,
        isbn,
        store_time,
        status
    FROM inventory
    WHERE isbn = p_isbn
    ORDER BY id;
END$$

DELIMITER ;
