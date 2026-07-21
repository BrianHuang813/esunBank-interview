DELIMITER $$

DROP PROCEDURE IF EXISTS sp_borrow_create$$
CREATE PROCEDURE sp_borrow_create(
    IN p_user_id BIGINT UNSIGNED,
    IN p_inventory_id BIGINT UNSIGNED
)
BEGIN
    DECLARE v_inventory_found BOOLEAN DEFAULT TRUE;
    DECLARE v_inventory_status VARCHAR(20);
    DECLARE v_borrowing_id BIGINT UNSIGNED;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    IF NOT EXISTS (
        SELECT 1
        FROM users
        WHERE id = p_user_id
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30201,
                MESSAGE_TEXT = 'USER_NOT_FOUND';
    END IF;

    BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND
            SET v_inventory_found = FALSE;

        SELECT status
        INTO v_inventory_status
        FROM inventory
        WHERE id = p_inventory_id
        FOR UPDATE;
    END;

    IF NOT v_inventory_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30202,
                MESSAGE_TEXT = 'INVENTORY_NOT_FOUND';
    END IF;

    IF v_inventory_status <> 'AVAILABLE' THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30203,
                MESSAGE_TEXT = 'INVENTORY_NOT_AVAILABLE';
    END IF;

    UPDATE inventory
    SET status = 'BORROWED'
    WHERE id = p_inventory_id;

    INSERT INTO borrowing_records (
        user_id,
        inventory_id,
        borrowing_time
    ) VALUES (
        p_user_id,
        p_inventory_id,
        CURRENT_TIMESTAMP(6)
    );

    SET v_borrowing_id = LAST_INSERT_ID();

    COMMIT;

    SELECT
        br.id,
        br.user_id,
        br.inventory_id,
        br.borrowing_time,
        br.return_time,
        i.status AS inventory_status,
        b.isbn,
        b.name AS book_name,
        b.author
    FROM borrowing_records br
    JOIN inventory i ON i.id = br.inventory_id
    JOIN books b ON b.isbn = i.isbn
    WHERE br.id = v_borrowing_id;
END$$

DROP PROCEDURE IF EXISTS sp_borrow_return$$
CREATE PROCEDURE sp_borrow_return(
    IN p_user_id BIGINT UNSIGNED,
    IN p_borrowing_id BIGINT UNSIGNED
)
BEGIN
    DECLARE v_borrowing_found BOOLEAN DEFAULT TRUE;
    DECLARE v_record_user_id BIGINT UNSIGNED;
    DECLARE v_inventory_id BIGINT UNSIGNED;
    DECLARE v_return_time DATETIME(6);
    DECLARE v_inventory_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    BEGIN
        DECLARE CONTINUE HANDLER FOR NOT FOUND
            SET v_borrowing_found = FALSE;

        SELECT user_id, inventory_id, return_time
        INTO v_record_user_id, v_inventory_id, v_return_time
        FROM borrowing_records
        WHERE id = p_borrowing_id
        FOR UPDATE;
    END;

    IF NOT v_borrowing_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30204,
                MESSAGE_TEXT = 'BORROWING_NOT_FOUND';
    END IF;

    IF v_record_user_id <> p_user_id THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30205,
                MESSAGE_TEXT = 'BORROWING_FORBIDDEN';
    END IF;

    IF v_return_time IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30206,
                MESSAGE_TEXT = 'BORROWING_ALREADY_RETURNED';
    END IF;

    SELECT status
    INTO v_inventory_status
    FROM inventory
    WHERE id = v_inventory_id
    FOR UPDATE;

    IF v_inventory_status <> 'BORROWED' THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30207,
                MESSAGE_TEXT = 'INVENTORY_STATUS_INCONSISTENT';
    END IF;

    UPDATE borrowing_records
    SET return_time = CURRENT_TIMESTAMP(6)
    WHERE id = p_borrowing_id;

    UPDATE inventory
    SET status = 'AVAILABLE'
    WHERE id = v_inventory_id;

    COMMIT;

    SELECT
        br.id,
        br.user_id,
        br.inventory_id,
        br.borrowing_time,
        br.return_time,
        i.status AS inventory_status,
        b.isbn,
        b.name AS book_name,
        b.author
    FROM borrowing_records br
    JOIN inventory i ON i.id = br.inventory_id
    JOIN books b ON b.isbn = i.isbn
    WHERE br.id = p_borrowing_id;
END$$

DROP PROCEDURE IF EXISTS sp_borrow_list_by_user$$
CREATE PROCEDURE sp_borrow_list_by_user(
    IN p_user_id BIGINT UNSIGNED,
    IN p_status VARCHAR(10),
    IN p_offset INT UNSIGNED,
    IN p_limit INT UNSIGNED
)
BEGIN
    DECLARE v_status VARCHAR(10) DEFAULT UPPER(COALESCE(p_status, 'ALL'));
    DECLARE v_offset INT UNSIGNED DEFAULT COALESCE(p_offset, 0);
    DECLARE v_limit INT UNSIGNED DEFAULT COALESCE(p_limit, 20);

    IF v_status NOT IN ('ACTIVE', 'RETURNED', 'ALL') THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30208,
                MESSAGE_TEXT = 'INVALID_BORROWING_STATUS';
    END IF;

    IF v_limit < 1 OR v_limit > 100 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30209,
                MESSAGE_TEXT = 'INVALID_PAGE_SIZE';
    END IF;

    SELECT
        br.id,
        br.user_id,
        br.inventory_id,
        br.borrowing_time,
        br.return_time,
        i.status AS inventory_status,
        b.isbn,
        b.name AS book_name,
        b.author,
        COUNT(*) OVER () AS total_items
    FROM borrowing_records br
    JOIN inventory i ON i.id = br.inventory_id
    JOIN books b ON b.isbn = i.isbn
    WHERE br.user_id = p_user_id
      AND (
          v_status = 'ALL'
          OR (v_status = 'ACTIVE' AND br.return_time IS NULL)
          OR (v_status = 'RETURNED' AND br.return_time IS NOT NULL)
      )
    ORDER BY br.borrowing_time DESC, br.id DESC
    LIMIT v_limit OFFSET v_offset;
END$$

DELIMITER ;
