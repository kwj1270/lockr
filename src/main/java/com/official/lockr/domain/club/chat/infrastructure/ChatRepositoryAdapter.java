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

    @Override
    public Optional<Chat> findById(final String chatId) {
        return jooqChatRepository.findById(chatId);
    }

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

    /**
     * 특정 메시지 ID 이후의 메시지 조회 (SSE 재연결 시 누락 메시지 복구용)
     */
    @Override
    public List<Chat> findAllAfterChatId(final String chatRoomId, final String afterChatId, final int limit) {
        try {
            final List<Chat> cachedChats = redisChatRepository.findAllAfterChatId(chatRoomId, afterChatId, limit);
            if (!cachedChats.isEmpty()) {
                log.debug("Cache hit for afterChatId query. chatRoomId={}, afterChatId={}", chatRoomId, afterChatId);
                return cachedChats;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch from Redis cache for afterChatId query, falling back to DB. chatRoomId={}, afterChatId={}",
                    chatRoomId, afterChatId, e);
        }

        log.debug("Cache miss for afterChatId query, fetching from DB. chatRoomId={}, afterChatId={}", chatRoomId, afterChatId);
        final List<Chat> dbChats = jooqChatRepository.findAllAfterChatId(chatRoomId, afterChatId, limit);

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
                log.warn("Failed to warm Redis cache for afterChatId query. chatRoomId={}, afterChatId={}",
                        chatRoomId, afterChatId, e);
            }
        }

        return dbChats;
    }

    @Override
    public void softDelete(final String chatId) {
        jooqChatRepository.softDelete(chatId);
        // Redis 캐시는 TTL에 의해 자연 만료됨
    }
}
