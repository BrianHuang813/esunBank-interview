SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;

SOURCE /docker-entrypoint-initdb.d/ddl/001_create_tables.sql;
SOURCE /docker-entrypoint-initdb.d/procedures/001_user_procedures.sql;
SOURCE /docker-entrypoint-initdb.d/procedures/002_book_procedures.sql;
SOURCE /docker-entrypoint-initdb.d/procedures/003_borrowing_procedures.sql;
SOURCE /docker-entrypoint-initdb.d/dml/001_seed_books.sql;
