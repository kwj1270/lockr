-- ============================================================
-- V6: 채팅 테이블 (chat_rooms, chatters, chats, chat_pinned_messages)
-- ============================================================

-- 채팅방
CREATE TABLE chat_rooms
(
    id         VARCHAR(255) NOT NULL COMMENT '채팅방 식별키' PRIMARY KEY,
    club_id    VARCHAR(255) NOT NULL COMMENT '클럽 식별키',
    name       VARCHAR(255) NOT NULL COMMENT '채팅방 이름',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '변경 시각',
    deleted_at DATETIME(6)  NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '채팅방';

-- 채팅 참여자
CREATE TABLE chatters
(
    id                VARCHAR(255) NOT NULL COMMENT '참여자 식별키' PRIMARY KEY,
    chat_room_id      VARCHAR(255) NOT NULL COMMENT '채팅방 식별키',
    user_id           VARCHAR(255) NOT NULL COMMENT '사용자 식별키',
    last_read_chat_id VARCHAR(255) NULL COMMENT '마지막으로 읽은 채팅 메시지 ID',
    UNIQUE KEY uk_chatters_room_user (chat_room_id, user_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '채팅 참여자';

-- 채팅 메시지
CREATE TABLE chats
(
    id                 VARCHAR(255)  NOT NULL COMMENT '메시지 식별키' PRIMARY KEY,
    chat_room_id       VARCHAR(255)  NOT NULL COMMENT '채팅방 식별키',
    sender_id          VARCHAR(255)  NOT NULL COMMENT '발신자 식별키',
    sender_name        VARCHAR(255)  NULL COMMENT '발신자 이름',
    message            VARCHAR(1000) NOT NULL COMMENT '메시지 내용',
    replied_to_id      VARCHAR(255)  NULL COMMENT '답장한 원본 메시지 ID',
    quoted_sender_name VARCHAR(255)  NULL COMMENT '인용된 메시지 발신자',
    quoted_content     VARCHAR(500)  NULL COMMENT '인용된 메시지 내용',
    created_at         DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    deleted_at         DATETIME(6)   NULL COMMENT '삭제 시각'
) COLLATE = utf8mb4_unicode_ci COMMENT = '채팅 메시지';

CREATE INDEX idx_chats_room_created ON chats (chat_room_id, created_at DESC);
CREATE INDEX idx_chats_replied_to ON chats (replied_to_id);

-- 고정 메시지
CREATE TABLE chat_pinned_messages
(
    id           VARCHAR(255) NOT NULL COMMENT '고정 메시지 식별키' PRIMARY KEY,
    chat_room_id VARCHAR(255) NOT NULL COMMENT '채팅방 식별키',
    chat_id      VARCHAR(255) NOT NULL COMMENT '메시지 식별키',
    pinned_by    VARCHAR(255) NOT NULL COMMENT '고정한 사용자 식별키',
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시각',
    UNIQUE KEY uk_chat_pinned_room_chat (chat_room_id, chat_id)
) COLLATE = utf8mb4_unicode_ci COMMENT = '고정 메시지';

CREATE INDEX idx_chat_pinned_messages_room ON chat_pinned_messages (chat_room_id);
