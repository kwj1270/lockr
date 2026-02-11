package com.official.lockr.domain.club.chat.infrastructure;

import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import org.apache.logging.log4j.util.Strings;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ChatsDao;
import org.jooq.generated.tables.pojos.ChatsEntity;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
                .and(DSL.field("deleted_at").isNull())
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
                        CHATS.ID.lessThan(lastChatId),
                        DSL.field("deleted_at").isNull()
                )
                .orderBy(CHATS.CREATED_AT.desc())
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(chats);
        return chats;
    }

    private List<Chat> findAllByChatRoomId(final String chatRoomId, final int limit) {
        List<Chat> chats = chatsDao.ctx()
                .selectFrom(CHATS)
                .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                .and(DSL.field("deleted_at").isNull())
                .orderBy(CHATS.CREATED_AT.desc())
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(chats);
        return chats;
    }

    @Override
    public List<Chat> findAllAfterChatId(final String chatRoomId, final String afterChatId, final int limit) {
        return chatsDao.ctx()
                .selectFrom(CHATS)
                .where(
                        CHATS.CHAT_ROOM_ID.eq(chatRoomId),
                        CHATS.ID.greaterThan(afterChatId),
                        DSL.field("deleted_at").isNull()
                )
                .orderBy(CHATS.CREATED_AT.asc())
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    @Override
    public void softDelete(final String chatId) {
        chatsDao.ctx()
                .update(CHATS)
                .set(DSL.field("deleted_at", LocalDateTime.class), LocalDateTime.now())
                .where(CHATS.ID.eq(chatId))
                .execute();
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
