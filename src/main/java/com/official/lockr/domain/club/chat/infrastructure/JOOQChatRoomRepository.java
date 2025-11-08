package com.official.lockr.domain.club.chat.infrastructure;

import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import com.official.lockr.domain.club.chat.domain.Chatter;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.util.UlidUtils;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ChatRoomsDao;
import org.jooq.generated.tables.daos.ChattersDao;
import org.jooq.generated.tables.pojos.ChatRoomsEntity;
import org.jooq.generated.tables.pojos.ChattersEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.ChatRoomsJOOQEntity.CHAT_ROOMS;
import static org.jooq.generated.tables.ChattersJOOQEntity.CHATTERS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQChatRoomRepository implements ChatRoomRepository {

    private final ChatRoomsDao chatRoomsDao;
    private final ChattersDao chattersDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQChatRoomRepository(final Configuration configuration,
                                  final DomainEventPublisher domainEventPublisher) {
        this.chatRoomsDao = new ChatRoomsDao(configuration);
        this.chattersDao = new ChattersDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
    }

    @Nullable
    @Override
    public ChatRoom findById(final String id) {
        final ChatRoomsEntity entity = chatRoomsDao.ctx()
                .selectFrom(CHAT_ROOMS)
                .where(CHAT_ROOMS.ID.eq(id))
                .fetchOneInto(ChatRoomsEntity.class);

        if (entity == null) {
            return null;
        }

        return toDomain(entity);
    }

    @Override
    public List<ChatRoom> findAllByClubId(final String clubId) {
        final List<ChatRoomsEntity> entities = chatRoomsDao.ctx()
                .selectFrom(CHAT_ROOMS)
                .where(CHAT_ROOMS.CLUB_ID.eq(clubId))
                .fetchInto(ChatRoomsEntity.class);

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    @Override
    public ChatRoom save(final ChatRoom chatRoom) {
        upsertChatRoom(chatRoom);
        syncChatters(chatRoom);
        chatRoom.publish(domainEventPublisher);
        return chatRoom;
    }

    @Transactional
    @Override
    public void delete(final String id) {
        chatRoomsDao.ctx()
                .deleteFrom(CHAT_ROOMS)
                .where(CHAT_ROOMS.ID.eq(id))
                .execute();
    }

    @Override
    public ChatRoom findByClubId(final String clubId) {
        return chatRoomsDao.ctx()
                .selectFrom(CHAT_ROOMS)
                .where(CHAT_ROOMS.CLUB_ID.eq(clubId))
                .fetchOptionalInto(ChatRoomsEntity.class)
                .map(this::toDomain)
                .orElse(null);
    }

    private void upsertChatRoom(final ChatRoom chatRoom) {
        chatRoomsDao.ctx()
                .insertInto(CHAT_ROOMS)
                .set(CHAT_ROOMS.ID, chatRoom.getId())
                .set(CHAT_ROOMS.CLUB_ID, chatRoom.getClubId())
                .set(CHAT_ROOMS.NAME, chatRoom.getName())
                .set(CHAT_ROOMS.CREATED_AT, chatRoom.getCreatedAt())
                .set(CHAT_ROOMS.UPDATED_AT, chatRoom.getUpdatedAt())
                .set(CHAT_ROOMS.DELETED_AT, chatRoom.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(CHAT_ROOMS.NAME, excluded(CHAT_ROOMS.NAME))
                .set(CHAT_ROOMS.UPDATED_AT, excluded(CHAT_ROOMS.UPDATED_AT))
                .set(CHAT_ROOMS.DELETED_AT, excluded(CHAT_ROOMS.DELETED_AT))
                .execute();
    }

    /**
     * 채팅방 멤버 동기화 (차등 업데이트)
     * - 추가된 멤버만 INSERT
     * - 삭제된 멤버만 DELETE
     * - 기존 멤버는 그대로 유지
     */
    private void syncChatters(final ChatRoom chatRoom) {
        final String chatRoomId = chatRoom.getId();

        // 1. 현재 DB의 멤버 목록 조회
        final List<String> existingUserIds = chattersDao.ctx()
                .select(CHATTERS.USER_ID)
                .from(CHATTERS)
                .where(CHATTERS.CHAT_ROOM_ID.eq(chatRoomId))
                .fetch(CHATTERS.USER_ID);

        // 2. 도메인의 멤버 목록
        final List<String> newUserIds = chatRoom.getChatters().stream()
                .map(Chatter::getUserId)
                .collect(Collectors.toCollection(ArrayList::new));

        // 3. 추가할 멤버 (newUserIds - existingUserIds)
        final List<String> toAdd = newUserIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .collect(Collectors.toCollection(ArrayList::new));

        // 4. 삭제할 멤버 (existingUserIds - newUserIds)
        final List<String> toRemove = existingUserIds.stream()
                .filter(userId -> !newUserIds.contains(userId))
                .collect(Collectors.toCollection(ArrayList::new));

        // 5. 삭제 실행
        if (!toRemove.isEmpty()) {
            chattersDao.ctx()
                    .deleteFrom(CHATTERS)
                    .where(CHATTERS.CHAT_ROOM_ID.eq(chatRoomId))
                    .and(CHATTERS.USER_ID.in(toRemove))
                    .execute();
        }

        // 6. 추가 실행
        if (!toAdd.isEmpty()) {
            var query = chattersDao.ctx().insertInto(CHATTERS, CHATTERS.ID, CHATTERS.CHAT_ROOM_ID, CHATTERS.USER_ID);
            for (final String userId : toAdd) {
                query.values(UlidUtils.generateUlid(), chatRoomId, userId);
            }
            query.execute();
        }
    }

    private ChatRoom toDomain(final ChatRoomsEntity entity) {
        return new ChatRoom(
                entity.getId(),
                entity.getClubId(),
                entity.getName(),
                findChattersByChatRoomId(entity.getId()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }

    private List<Chatter> findChattersByChatRoomId(final String chatRoomId) {
        return chattersDao.ctx()
                .selectFrom(CHATTERS)
                .where(CHATTERS.CHAT_ROOM_ID.eq(chatRoomId))
                .fetchInto(ChattersEntity.class)
                .stream()
                .map(entity -> new Chatter(entity.getUserId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
