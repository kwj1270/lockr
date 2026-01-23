package com.official.lockr.domain.club.chat.infrastructure;

import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.apache.logging.log4j.util.Strings;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ChatsDao;
import org.jooq.generated.tables.pojos.ChatsEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.ChatsJOOQEntity.CHATS;

@Repository
public class JOOQChatRepository implements ChatRepository {

    private final ChatsDao chatsDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQChatRepository(final Configuration configuration,
                              final DomainEventPublisher domainEventPublisher
    ) {
        this.chatsDao = new ChatsDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional
    @Override
    public Chat save(final Chat chat) {
        chatsDao.ctx()
                .insertInto(CHATS)
                .set(CHATS.ID, chat.getId())
                .set(CHATS.CHAT_ROOM_ID, chat.getChatRoomId())
                .set(CHATS.SENDER_ID, chat.getSenderId())
                .set(CHATS.SENDER_NAME, chat.getSenderName())
                .set(CHATS.MESSAGE, chat.getMessage())
                .set(CHATS.REPLIED_TO_ID, chat.getRepliedToId())
                .set(CHATS.QUOTED_SENDER_NAME, chat.getQuotedSenderName())
                .set(CHATS.QUOTED_CONTENT, chat.getQuotedContent())
                .set(CHATS.CREATED_AT, chat.getCreatedAt())
                .execute();
        chat.publish(domainEventPublisher);
        return chat;
    }

    @Override
    public Optional<Chat> findById(final String chatId) {
        return chatsDao.ctx()
                .selectFrom(CHATS)
                .where(CHATS.ID.eq(chatId))
                .fetchOptionalInto(ChatsEntity.class)
                .map(this::toDomain);
    }

    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId) {
        return chatsDao.ctx()
                .selectFrom(CHATS)
                .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                .orderBy(CHATS.CREATED_AT.asc())
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId, final String lastChatId, final int limit) {
        if (Strings.isBlank(lastChatId)) {
            return findAllByChatRoomId(chatRoomId, limit);
        }
        List<Chat> chats = chatsDao.ctx()
                .selectFrom(CHATS)
                .where(
                        CHATS.CHAT_ROOM_ID.eq(chatRoomId),
                        CHATS.ID.lessThan(lastChatId)  // ULID 문자열 비교로 시간 순서 보장 (위로 스크롤 시 더 오래된 메시지)
                )
                .orderBy(CHATS.CREATED_AT.desc())  // 최신순으로 조회 (limit 적용 후 reverse)
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(chats);  // [오래된→최신] 순서로 반환
        return chats;
    }

    private List<Chat> findAllByChatRoomId(final String chatRoomId, final int limit) {
        List<Chat> chats = chatsDao.ctx()
                .selectFrom(CHATS)
                .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                .orderBy(CHATS.CREATED_AT.desc())  // 최신순으로 조회
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(chats);  // [오래된→최신] 순서로 반환
        return chats;
    }

    private Chat toDomain(final ChatsEntity entity) {
        return new Chat(
                entity.getId(),
                entity.getChatRoomId(),
                entity.getSenderId(),
                entity.getSenderName(),
                entity.getMessage(),
                entity.getRepliedToId(),
                entity.getQuotedSenderName(),
                entity.getQuotedContent(),
                entity.getCreatedAt()
        );
    }
}
