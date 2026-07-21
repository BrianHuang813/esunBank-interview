DELIMITER $$

DROP PROCEDURE IF EXISTS sp_user_create$$
CREATE PROCEDURE sp_user_create(
    IN p_phone_number VARCHAR(20),
    IN p_password_hash VARCHAR(100),
    IN p_user_name VARCHAR(100)
)
BEGIN
    DECLARE EXIT HANDLER FOR 1062
    BEGIN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30004,
                MESSAGE_TEXT = 'PHONE_NUMBER_ALREADY_EXISTS';
    END;

    IF p_phone_number IS NULL OR p_phone_number NOT REGEXP '^09[0-9]{8}$' THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30001,
                MESSAGE_TEXT = 'INVALID_PHONE_NUMBER';
    END IF;

    IF p_password_hash IS NULL OR CHAR_LENGTH(p_password_hash) < 20 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30002,
                MESSAGE_TEXT = 'INVALID_PASSWORD_HASH';
    END IF;

    IF p_user_name IS NULL OR CHAR_LENGTH(TRIM(p_user_name)) = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30003,
                MESSAGE_TEXT = 'INVALID_USER_NAME';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM users
        WHERE phone_number = p_phone_number
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30004,
                MESSAGE_TEXT = 'PHONE_NUMBER_ALREADY_EXISTS';
    END IF;

    INSERT INTO users (phone_number, password_hash, user_name)
    VALUES (p_phone_number, p_password_hash, TRIM(p_user_name));

    SELECT
        id,
        phone_number,
        user_name,
        registration_time,
        last_login_time
    FROM users
    WHERE id = LAST_INSERT_ID();
END$$

DROP PROCEDURE IF EXISTS sp_user_find_by_phone$$
CREATE PROCEDURE sp_user_find_by_phone(
    IN p_phone_number VARCHAR(20)
)
BEGIN
    SELECT
        id,
        phone_number,
        password_hash,
        user_name,
        registration_time,
        last_login_time
    FROM users
    WHERE phone_number = p_phone_number;
END$$

DROP PROCEDURE IF EXISTS sp_user_record_login$$
CREATE PROCEDURE sp_user_record_login(
    IN p_user_id BIGINT UNSIGNED
)
BEGIN
    UPDATE users
    SET last_login_time = CURRENT_TIMESTAMP(6)
    WHERE id = p_user_id;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 30005,
                MESSAGE_TEXT = 'USER_NOT_FOUND';
    END IF;

    SELECT
        id,
        phone_number,
        user_name,
        registration_time,
        last_login_time
    FROM users
    WHERE id = p_user_id;
END$$

DELIMITER ;
