package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.chat.api.dto.AddChatterRequest;
import com.official.lockr.domain.club.chat.api.dto.SendMessageRequest;
import com.official.lockr.domain.club.chat.application.usecase.AddChatterUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetChatRoomsUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetMessagesUseCase;
import com.official.lockr.domain.club.chat.application.usecase.SendMessageUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.infrastructure.sse.SseChatEventPublisher;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/chats")
@RestController
public class ChatApi {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetMessagesUseCase getMessagesUseCase;
    private final AddChatterUseCase addChatterUseCase;
    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final SseChatEventPublisher sseEventPublisher;

    public ChatApi(final SendMessageUseCase sendMessageUseCase,
                   final AddChatterUseCase addChatterUseCase,
                   final GetMessagesUseCase getMessagesUseCase,
                   final GetChatRoomsUseCase getChatRoomsUseCase,
                   final SseChatEventPublisher sseEventPublisher) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.getMessagesUseCase = getMessagesUseCase;
        this.addChatterUseCase = addChatterUseCase;
        this.getChatRoomsUseCase = getChatRoomsUseCase;
        this.sseEventPublisher = sseEventPublisher;
    }

    @PostMapping("/rooms/{chatRoomId}/chatters")
    public ResponseEntity<ChatRoom> addChatter(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @RequestBody final AddChatterRequest request
    ) {
        session(httpSession);
        final ChatRoom chatRoom = addChatterUseCase.addChatter(request.toCommand(clubId, chatRoomId));
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<Chat> sendMessage(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @RequestBody final SendMessageRequest request
    ) {
        final SignInSession session = session(httpSession);
        final Chat chat = sendMessageUseCase.send(request.toCommand(clubId, chatRoomId, session.userId()));
        return ResponseEntity.ok(chat);
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new IllegalStateException("User not authenticated");
        }
        return signIn;
    }
}
