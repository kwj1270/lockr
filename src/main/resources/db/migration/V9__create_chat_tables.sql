CREATE TABLE IF NOT EXISTS chat_rooms
(
    id         VARCHAR(255) PRIMARY KEY,
    club_id    VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL,
    updated_at DATETIME     NOT NULL,
    deleted_at DATETIME
) COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chatters
(
    id           VARCHAR(255) PRIMARY KEY,
    chat_room_id VARCHAR(255) NOT NULL,
    user_id      VARCHAR(255) NOT NULL,
    UNIQUE KEY unique_chat_room_user (chat_room_id, user_id)
) COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chats
(
    id              VARCHAR(255) PRIMARY KEY,
    chat_room_id    VARCHAR(255)  NOT NULL,
    sender_id       VARCHAR(255)  NOT NULL,
    sender_nickname VARCHAR(255)  NOT NULL,
    message         VARCHAR(1000) NOT NULL,
    created_at      DATETIME      NOT NULL,
    INDEX idx_chat_room_created (chat_room_id, created_at DESC)
) COLLATE = utf8mb4_unicode_ci;
