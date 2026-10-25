CREATE TABLE one_time_tokens (
    token_value VARCHAR(36)  NOT NULL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    expires_at  DATETIME(6)  NOT NULL
);

CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE read_books (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT       NOT NULL,
    title    VARCHAR(255) NOT NULL,
    added_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_rb_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_rb_user_title UNIQUE (user_id, title)
);

CREATE TABLE recommendations (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    title       VARCHAR(255)  NOT NULL,
    author      VARCHAR(255),
    description VARCHAR(1000),
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_rec_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE saved_books (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT       NOT NULL,
    title    VARCHAR(255) NOT NULL,
    author   VARCHAR(255),
    saved_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_sb_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_sb_user_title UNIQUE (user_id, title)
);
