-- 메시지 고정 테이블
CREATE TABLE IF NOT EXISTS chat_pinned_messages
(
    id           VARCHAR(255) PRIMARY KEY,
    chat_room_id VARCHAR(255) NOT NULL,
    chat_id      VARCHAR(255) NOT NULL,
    pinned_by    VARCHAR(255) NOT NULL,
    created_at   DATETIME     NOT NULL,
    UNIQUE KEY unique_chat_room_chat (chat_room_id, chat_id),
    INDEX idx_pinned_chat_room (chat_room_id)
) COLLATE = utf8mb4_unicode_ci;

-- 메시지 soft delete 컬럼 추가
ALTER TABLE chats ADD COLUMN deleted_at DATETIME NULL COMMENT '삭제 시각 (soft delete)';
