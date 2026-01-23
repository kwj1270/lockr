package com.official.lockr.domain.club.chat.infrastructure;

import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 채팅 저장소 어댑터
 * - 캐시 전략: Cache-Aside + Write-Through 패턴
 * - 주 저장소: JOOQ (PostgreSQL/MySQL)
 * - 캐시: Redis (선택적, 장애 시 DB로 폴백)
 *
 * 쓰기 전략 (Write-Through):
 * 1. DB에 먼저 저장 (Source of Truth)
 * 2. 성공 시 Redis에 캐시 저장 (실패해도 무시)
 *
 * 읽기 전략 (Cache-Aside with Warming):
 * 1. Redis 캐시 조회
 * 2. 캐시 미스 시 DB 조회
 * 3. DB 조회 결과를 Redis에 저장 (Cache Warming)
 */
@Component
public class ChatRepositoryAdapter implements ChatRepository {

    private static final Logger log = LoggerFactory.getLogger(ChatRepositoryAdapter.class);

    private final JOOQChatRepository jooqChatRepository;
    private final RedisChatRepository redisChatRepository;

    public ChatRepositoryAdapter(
            final JOOQChatRepository jooqChatRepository,
            final RedisChatRepository redisChatRepository) {
        this.jooqChatRepository = jooqChatRepository;
        this.redisChatRepository = redisChatRepository;
    }

    /**
     * Write-Through 패턴: DB 먼저 저장 후 캐시 업데이트
     */
    @Override
    public Chat save(final Chat chat) {
        final Chat savedChat = jooqChatRepository.save(chat);
        try {
            redisChatRepository.save(savedChat);
        } catch (Exception e) {
            log.warn("Failed to save chat to Redis cache, but DB save was successful. chatId={}, chatRoomId={}", savedChat.getId(), savedChat.getChatRoomId(), e);
        }
        return savedChat;
    }

    /**
     * ID로 채팅 메시지 조회 (DB 직접 조회)
     */
    @Override
    public Optional<Chat> findById(final String chatId) {
        return jooqChatRepository.findById(chatId);
    }

    /**
     * Cache-Aside 패턴: 캐시 조회 → 미스 시 DB 조회 → 캐시 워밍
     */
    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId) {
        try {
            final List<Chat> cachedChats = redisChatRepository.findAllByChatRoomId(chatRoomId);
            if (!cachedChats.isEmpty()) {
                log.debug("Cache hit for chatRoomId={}", chatRoomId);
                return cachedChats;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from Redis cache, falling back to DB. chatRoomId={}", chatRoomId, e);
        }

        log.debug("Cache miss for chatRoomId={}, fetching from DB", chatRoomId);
        final List<Chat> dbChats = jooqChatRepository.findAllByChatRoomId(chatRoomId);

        if (!dbChats.isEmpty()) {
            try {
                dbChats.forEach(chat -> {
                    try {
                        redisChatRepository.save(chat);
                    } catch (Exception e) {
                        log.debug("Failed to warm cache for chat. chatId={}", chat.getId(), e);
                    }
                });
            } catch (Exception e) {
                log.warn("Failed to warm Redis cache. chatRoomId={}", chatRoomId, e);
            }
        }

        return dbChats;
    }

    /**
     * Cache-Aside 패턴: 커서 페이지네이션 (ULID 기반)
     */
    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId, final String lastChatId, final int limit) {
        try {
            final List<Chat> cachedChats = redisChatRepository.findAllByChatRoomId(chatRoomId, lastChatId, limit);
            if (!cachedChats.isEmpty()) {
                log.debug("Cache hit for chatRoomId={}, lastChatId={}, limit={}", chatRoomId, lastChatId, limit);
                return cachedChats;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from Redis cache, falling back to DB. chatRoomId={}, lastChatId={}, limit={}",
                    chatRoomId, lastChatId, limit, e);
        }

        log.debug("Cache miss for chatRoomId={}, lastChatId={}, limit={}, fetching from DB", chatRoomId, lastChatId, limit);
        final List<Chat> dbChats = jooqChatRepository.findAllByChatRoomId(chatRoomId, lastChatId, limit);

        if (!dbChats.isEmpty()) {
            try {
                dbChats.forEach(chat -> {
                    try {
                        redisChatRepository.save(chat);
                    } catch (Exception e) {
                        log.debug("Failed to warm cache for chat. chatId={}", chat.getId(), e);
                    }
                });
            } catch (Exception e) {
                log.warn("Failed to warm Redis cache. chatRoomId={}, lastChatId={}, limit={}",
                        chatRoomId, lastChatId, limit, e);
            }
        }

        return dbChats;
    }
}
