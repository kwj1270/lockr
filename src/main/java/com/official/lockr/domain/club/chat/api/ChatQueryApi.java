package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.chat.api.dto.ChatterProfileResponse;
import com.official.lockr.domain.club.chat.api.dto.UnreadCountResponse;
import com.official.lockr.domain.club.chat.application.usecase.GetChatRoomsUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetMessagesUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetPinnedMessagesUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import com.official.lockr.domain.club.chat.domain.PinnedMessage;
import com.official.lockr.domain.club.chat.infrastructure.sse.SseChatEventPublisher;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.ChattersDao;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.jooq.impl.DSL;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;
import static org.jooq.generated.tables.ChatsJOOQEntity.CHATS;
import static org.jooq.generated.tables.ChattersJOOQEntity.CHATTERS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;

@RequestMapping("/api/v1/clubs/{clubId}/chats")
@RestController
public class ChatQueryApi {

    private final GetMessagesUseCase getMessagesUseCase;
    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final GetPinnedMessagesUseCase getPinnedMessagesUseCase;
    private final SseChatEventPublisher sseEventPublisher;
    private final ChatRoomRepository chatRoomRepository;
    private final ChattersDao chattersDao;

    public ChatQueryApi(
            final GetMessagesUseCase getMessagesUseCase,
            final GetChatRoomsUseCase getChatRoomsUseCase,
            final GetPinnedMessagesUseCase getPinnedMessagesUseCase,
            final SseChatEventPublisher sseEventPublisher,
            final ChatRoomRepository chatRoomRepository,
            final Configuration configuration
    ) {
        this.getMessagesUseCase = getMessagesUseCase;
        this.getChatRoomsUseCase = getChatRoomsUseCase;
        this.getPinnedMessagesUseCase = getPinnedMessagesUseCase;
        this.sseEventPublisher = sseEventPublisher;
        this.chatRoomRepository = chatRoomRepository;
        this.chattersDao = new ChattersDao(configuration);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId
    ) {
        final List<ChatRoom> chatRooms = getChatRoomsUseCase.getChatRooms(clubId, signInSession.userId());
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<Chat>> getMessages(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @RequestParam(required = false, defaultValue = "") final String lastChatId,
            @RequestParam(required = false, defaultValue = "") final String afterChatId,
            @RequestParam(required = false, defaultValue = "100") final int limit
    ) {
        final List<Chat> messages;
        if (!afterChatId.isEmpty()) {
            messages = getMessagesUseCase.getMessagesAfter(chatRoomId, signInSession.userId(), afterChatId, limit);
        } else {
            messages = getMessagesUseCase.getMessages(chatRoomId, signInSession.userId(), lastChatId, limit);
        }
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/rooms/{chatRoomId}/pinned-messages")
    public ResponseEntity<List<PinnedMessage>> getPinnedMessages(
            @RequestAttribute("signInSession") final SignInSession session,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId
    ) {
        final List<PinnedMessage> pinnedMessages = getPinnedMessagesUseCase.getPinnedMessages(chatRoomId, session.userId());
        return ResponseEntity.ok(pinnedMessages);
    }

    @GetMapping(value = "/test-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testStream(@PathVariable final String clubId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("test"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToClubChats(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId
    ) {
        final String userId = signInSession.userId();
        final List<ChatRoom> chatRooms = chatRoomRepository.findAllByClubId(clubId);
        final boolean isMember = chatRooms.stream()
                .anyMatch(room -> room.hasMember(userId));
        if (!isMember) {
            throw new IllegalArgumentException("User is not a member of any chat room in club: " + userId);
        }
        return sseEventPublisher.subscribeToClub(clubId);
    }

    @GetMapping(value = "/rooms/{chatRoomId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToChatRoom(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId
    ) {
        final String userId = signInSession.userId();
        final ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);
        if (isNull(chatRoom)) {
            throw new IllegalArgumentException("ChatRoom not found: " + chatRoomId);
        }
        if (!chatRoom.hasMember(userId)) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + userId);
        }
        return sseEventPublisher.subscribeToChatRoom(chatRoomId);
    }

    @GetMapping("/rooms/{chatRoomId}/chatters")
    public ResponseEntity<List<ChatterProfileResponse>> getChatters(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId
    ) {
        final List<ChatterProfileResponse> chatters = chattersDao.ctx()
                .select(
                        CHATTERS.USER_ID,
                        USER_ADDITIONAL_INFO.NAME,
                        MEMBERS.PROFILE_IMAGE
                )
                .from(CHATTERS)
                .leftJoin(MEMBERS).on(CHATTERS.USER_ID.eq(MEMBERS.USER_ID)
                        .and(MEMBERS.CLUB_ID.eq(clubId))
                        .and(MEMBERS.DELETED_AT.isNull()))
                .leftJoin(USER_ADDITIONAL_INFO).on(CHATTERS.USER_ID.eq(USER_ADDITIONAL_INFO.USER_ID))
                .where(CHATTERS.CHAT_ROOM_ID.eq(chatRoomId))
                .fetch()
                .map(record -> new ChatterProfileResponse(
                        record.get(CHATTERS.USER_ID),
                        record.get(USER_ADDITIONAL_INFO.NAME),
                        record.get(MEMBERS.PROFILE_IMAGE)
                ));

        return ResponseEntity.ok(chatters);
    }

    @GetMapping("/rooms/{chatRoomId}/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId
    ) {
        final String userId = signInSession.userId();

        final String lastReadChatId = chattersDao.ctx()
                .select(DSL.field("last_read_chat_id", String.class))
                .from(CHATTERS)
                .where(CHATTERS.CHAT_ROOM_ID.eq(chatRoomId))
                .and(CHATTERS.USER_ID.eq(userId))
                .fetchOneInto(String.class);

        final long count;
        if (lastReadChatId == null) {
            count = chattersDao.ctx()
                    .selectCount()
                    .from(CHATS)
                    .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                    .and(DSL.field("deleted_at").isNull())
                    .fetchOneInto(Long.class);
        } else {
            count = chattersDao.ctx()
                    .selectCount()
                    .from(CHATS)
                    .where(CHATS.CHAT_ROOM_ID.eq(chatRoomId))
                    .and(CHATS.ID.greaterThan(lastReadChatId))
                    .and(DSL.field("deleted_at").isNull())
                    .fetchOneInto(Long.class);
        }

        return ResponseEntity.ok(new UnreadCountResponse(count));
    }
}
