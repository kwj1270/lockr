package com.official.lockr.domain.club.chat.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRepository;
import io.jsonwebtoken.lang.Collections;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisChatRepository implements ChatRepository {

    private static final String CHAT_KEY_PREFIX = "chat:room:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final long cacheTtlHours;

    public RedisChatRepository(
            final RedisTemplate<String, String> redisTemplate,
            @Value("${chat.cache.ttl-hours:24}") final long cacheTtlHours) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.cacheTtlHours = cacheTtlHours;
    }

    @Override
    public Chat save(final Chat chat) {
        try {
            final String key = getChatRoomKey(chat.getChatRoomId());
            final String value = objectMapper.writeValueAsString(new ChatDto(chat));
            final double score = toTimestamp(chat.getCreatedAt());

            redisTemplate.opsForZSet().add(key, value, score);
            redisTemplate.expire(key, cacheTtlHours, TimeUnit.HOURS);

            return chat;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chat message", e);
        }
    }

    @Override
    public Optional<Chat> findById(final String chatId) {
        // Redis에서는 개별 메시지 조회가 비효율적이므로 Optional.empty() 반환
        // ChatRepositoryAdapter에서 DB 조회로 폴백됨
        return Optional.empty();
    }

    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId) {
        final String key = getChatRoomKey(chatRoomId);
        final Set<String> messages = redisTemplate.opsForZSet().reverseRange(key, 0, -1);

        if (Collections.isEmpty(messages)) {
            return List.of();
        }
        return messages.stream()
                .map(this::deserialize)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId, final String lastChatId, final int limit) {
        final String key = getChatRoomKey(chatRoomId);
        if (Strings.isBlank(lastChatId)) {
            return findAllChatRoomId(limit, key);
        }
        final Double lastScore = findScoreByMessageId(key, lastChatId);
        if (Objects.isNull(lastScore)) {
            return findAllChatRoomId(limit, key);
        }
        // lastChatId를 필터링으로 제거하므로 limit+1개를 가져옴
        final Set<String> messages = redisTemplate.opsForZSet()
                .reverseRangeByScore(key, Double.NEGATIVE_INFINITY, lastScore, 0, limit + 1);
        if (Collections.isEmpty(messages)) {
            return List.of();
        }
        return messages.stream()
                .map(this::deserialize)
                .filter(chat -> !chat.getId().equals(lastChatId)) // lastChatId 제외
                .limit(limit)  // 최종적으로 limit개만 반환
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Chat> findAllAfterChatId(final String chatRoomId, final String afterChatId, final int limit) {
        final String key = getChatRoomKey(chatRoomId);
        final Double afterScore = findScoreByMessageId(key, afterChatId);
        if (Objects.isNull(afterScore)) {
            return List.of();
        }
        // afterChatId 이후의 메시지를 오래된순으로 조회
        final Set<String> messages = redisTemplate.opsForZSet()
                .rangeByScore(key, afterScore, Double.POSITIVE_INFINITY, 0, limit + 1);
        if (Collections.isEmpty(messages)) {
            return List.of();
        }
        return messages.stream()
                .map(this::deserialize)
                .filter(chat -> !chat.getId().equals(afterChatId))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void softDelete(final String chatId) {
        // Redis는 캐시 용도이므로 TTL에 의해 자연 만료
        // 명시적 삭제가 필요하면 구현 가능하나, 현재는 no-op
    }

    private List<Chat> findAllChatRoomId(final int limit, final String key) {
        final Set<String> messages = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        if (messages == null) {
            return List.of();
        }
        return messages.stream()
                .map(this::deserialize)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * ULID 기반 메시지 ID로 해당 메시지의 score(timestamp) 찾기
     */
    private Double findScoreByMessageId(final String key, final String messageId) {
        final Set<String> allMessages = redisTemplate.opsForZSet().range(key, 0, -1);
        if (Collections.isEmpty(allMessages)) {
            return null;
        }

        for (String message : allMessages) {
            final Chat chat = deserialize(message);
            if (chat.getId().equals(messageId)) {
                return toTimestamp(chat.getCreatedAt());
            }
        }
        return null;
    }

    private String getChatRoomKey(final String chatRoomId) {
        return CHAT_KEY_PREFIX + chatRoomId;
    }

    private double toTimestamp(final LocalDateTime dateTime) {
        return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private Chat deserialize(final String json) {
        try {
            final ChatDto dto = objectMapper.readValue(json, ChatDto.class);
            return dto.toDomain();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize chat message", e);
        }
    }

    static class ChatDto {
        public String id;
        public String chatRoomId;
        public String senderId;
        public String message;
        public String repliedToId;
        public String quotedSenderName;
        public String quotedContent;
        public LocalDateTime createdAt;

        public ChatDto() {
        }

        public ChatDto(final Chat chat) {
            this.id = chat.getId();
            this.chatRoomId = chat.getChatRoomId();
            this.senderId = chat.getSenderId();
            this.message = chat.getMessage();
            this.repliedToId = chat.getRepliedToId();
            this.quotedSenderName = chat.getQuotedSenderName();
            this.quotedContent = chat.getQuotedContent();
            this.createdAt = chat.getCreatedAt();
        }

        public Chat toDomain() {
            return new Chat(
                    id, chatRoomId, senderId, message,
                    repliedToId, quotedSenderName, quotedContent, createdAt
            );
        }
    }
}
