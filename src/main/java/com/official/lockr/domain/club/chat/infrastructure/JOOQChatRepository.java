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

import java.util.List;
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
                .set(CHATS.SENDER_NICKNAME, chat.getSenderNickname())
                .set(CHATS.MESSAGE, chat.getMessage())
                .set(CHATS.CREATED_AT, chat.getCreatedAt())
                .execute();
        chat.publish(domainEventPublisher);
        return chat;
    }

    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId) {
        return chatsDao.ctx()
                .selectFrom(CHATS)
                .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                .orderBy(CHATS.CREATED_AT.desc())
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Chat> findAllByChatRoomId(final String chatRoomId, final String lastChatId, final int limit) {
        if (Strings.isBlank(lastChatId)) {
            return findAllByChatRoomId(chatRoomId, limit);
        }
        return chatsDao.ctx()
                .selectFrom(CHATS)
                .where(
                        CHATS.CHAT_ROOM_ID.eq(chatRoomId),
                        CHATS.ID.lessThan(lastChatId)  // ULID 문자열 비교로 시간 순서 보장
                )
                .orderBy(CHATS.CREATED_AT.desc())
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private List<Chat> findAllByChatRoomId(final String chatRoomId, final int limit) {
        return chatsDao.ctx()
                .selectFrom(CHATS)
                .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                .orderBy(CHATS.CREATED_AT.desc())
                .limit(limit)
                .fetchInto(ChatsEntity.class)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private Chat toDomain(final ChatsEntity entity) {
        return new Chat(
                entity.getId(),
                entity.getChatRoomId(),
                entity.getSenderId(),
                entity.getSenderNickname(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }
}
