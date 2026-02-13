package com.official.lockr.domain.club.chat.infrastructure;

import com.official.lockr.domain.club.chat.domain.PinnedMessage;
import com.official.lockr.domain.club.chat.domain.PinnedMessageRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class JOOQPinnedMessageRepository implements PinnedMessageRepository {

    private final DSLContext dsl;

    public JOOQPinnedMessageRepository(final DSLContext dsl) {
        this.dsl = dsl;
    }

    @Transactional
    @Override
    public PinnedMessage save(final PinnedMessage pinnedMessage) {
        dsl.insertInto(table("chat_pinned_messages"))
                .set(field("id"), pinnedMessage.getId())
                .set(field("chat_room_id"), pinnedMessage.getChatRoomId())
                .set(field("chat_id"), pinnedMessage.getChatId())
                .set(field("pinned_by"), pinnedMessage.getPinnedBy())
                .set(field("created_at"), pinnedMessage.getCreatedAt())
                .execute();
        return pinnedMessage;
    }

    @Transactional
    @Override
    public void deleteByChatRoomIdAndChatId(final String chatRoomId, final String chatId) {
        dsl.deleteFrom(table("chat_pinned_messages"))
                .where(field("chat_room_id").eq(chatRoomId))
                .and(field("chat_id").eq(chatId))
                .execute();
    }

    @Override
    public Optional<PinnedMessage> findByChatRoomIdAndChatId(final String chatRoomId, final String chatId) {
        return dsl.selectFrom(table("chat_pinned_messages"))
                .where(field("chat_room_id").eq(chatRoomId))
                .and(field("chat_id").eq(chatId))
                .fetchOptional()
                .map(this::toDomain);
    }

    @Override
    public List<PinnedMessage> findAllByChatRoomId(final String chatRoomId) {
        return dsl.selectFrom(table("chat_pinned_messages"))
                .where(field("chat_room_id").eq(chatRoomId))
                .orderBy(field("created_at").desc())
                .fetch()
                .map(this::toDomain);
    }

    private PinnedMessage toDomain(final Record record) {
        return new PinnedMessage(
                record.get(field("id", String.class)),
                record.get(field("chat_room_id", String.class)),
                record.get(field("chat_id", String.class)),
                record.get(field("pinned_by", String.class)),
                record.get(field("created_at", LocalDateTime.class))
        );
    }
}
