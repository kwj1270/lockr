package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.chat.application.usecase.GetChatRoomsUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetMessagesUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.infrastructure.sse.SseChatEventPublisher;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/chats")
@RestController
public class ChatQueryApi {

    private final GetMessagesUseCase getMessagesUseCase;
    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final SseChatEventPublisher sseEventPublisher;

    public ChatQueryApi(
            final GetMessagesUseCase getMessagesUseCase,
            final GetChatRoomsUseCase getChatRoomsUseCase,
            final SseChatEventPublisher sseEventPublisher
    ) {
        this.getMessagesUseCase = getMessagesUseCase;
        this.getChatRoomsUseCase = getChatRoomsUseCase;
        this.sseEventPublisher = sseEventPublisher;
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms(
            final HttpSession httpSession,
            @PathVariable final String clubId
    ) {
        final SignInSession session = session(httpSession);
        final List<ChatRoom> chatRooms = getChatRoomsUseCase.getChatRooms(clubId, session.userId());
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<List<Chat>> getMessages(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @RequestParam(required = false, defaultValue = "") final String lastChatId,
            @RequestParam(required = false, defaultValue = "100") final int limit
    ) {
        final SignInSession session = session(httpSession);
        final List<Chat> messages = getMessagesUseCase.getMessages(chatRoomId, session.userId(), lastChatId, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToClubChats(
            final HttpSession httpSession,
            @PathVariable final String clubId
    ) {
        session(httpSession);
        return sseEventPublisher.subscribeToClub(clubId);
    }

    @GetMapping(value = "/rooms/{chatRoomId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToChatRoom(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId
    ) {
        session(httpSession);
        return sseEventPublisher.subscribeToChatRoom(chatRoomId);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException("User not authenticated");
        }
        return signIn;
    }
}
