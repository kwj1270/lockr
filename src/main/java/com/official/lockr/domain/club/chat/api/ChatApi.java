package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.chat.api.dto.AddChatterRequest;
import com.official.lockr.domain.club.chat.api.dto.SendMessageRequest;
import com.official.lockr.domain.club.chat.application.command.DeleteMessageCommand;
import com.official.lockr.domain.club.chat.application.command.PinMessageCommand;
import com.official.lockr.domain.club.chat.application.command.UnpinMessageCommand;
import com.official.lockr.domain.club.chat.application.usecase.AddChatterUseCase;
import com.official.lockr.domain.club.chat.application.usecase.DeleteMessageUseCase;
import com.official.lockr.domain.club.chat.application.usecase.PinMessageUseCase;
import com.official.lockr.domain.club.chat.application.usecase.SendMessageUseCase;
import com.official.lockr.domain.club.chat.application.usecase.UnpinMessageUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.PinnedMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/chats")
@RestController
public class ChatApi {

    private final SendMessageUseCase sendMessageUseCase;
    private final AddChatterUseCase addChatterUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;
    private final PinMessageUseCase pinMessageUseCase;
    private final UnpinMessageUseCase unpinMessageUseCase;

    public ChatApi(final SendMessageUseCase sendMessageUseCase,
                   final AddChatterUseCase addChatterUseCase,
                   final DeleteMessageUseCase deleteMessageUseCase,
                   final PinMessageUseCase pinMessageUseCase,
                   final UnpinMessageUseCase unpinMessageUseCase) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.addChatterUseCase = addChatterUseCase;
        this.deleteMessageUseCase = deleteMessageUseCase;
        this.pinMessageUseCase = pinMessageUseCase;
        this.unpinMessageUseCase = unpinMessageUseCase;
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

    @DeleteMapping("/rooms/{chatRoomId}/messages/{chatId}")
    public ResponseEntity<Void> deleteMessage(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId
    ) {
        final SignInSession session = session(httpSession);
        deleteMessageUseCase.delete(new DeleteMessageCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{chatRoomId}/messages/{chatId}/pin")
    public ResponseEntity<PinnedMessage> pinMessage(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId
    ) {
        final SignInSession session = session(httpSession);
        final PinnedMessage pinnedMessage = pinMessageUseCase.pin(
                new PinMessageCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.ok(pinnedMessage);
    }

    @DeleteMapping("/rooms/{chatRoomId}/messages/{chatId}/pin")
    public ResponseEntity<Void> unpinMessage(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId
    ) {
        final SignInSession session = session(httpSession);
        unpinMessageUseCase.unpin(new UnpinMessageCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.noContent().build();
    }

    private SignInSession session(final HttpSession httpSession) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        if (isNull(signIn)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
