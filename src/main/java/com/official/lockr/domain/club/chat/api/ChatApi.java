package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.chat.api.dto.AddChatterRequest;
import com.official.lockr.domain.club.chat.api.dto.SendMessageRequest;
import com.official.lockr.domain.club.chat.application.usecase.AddChatterUseCase;
import com.official.lockr.domain.club.chat.application.usecase.SendMessageUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static java.util.Objects.isNull;

@RequestMapping("/api/v1/clubs/{clubId}/chats")
@RestController
public class ChatApi {

    private final SendMessageUseCase sendMessageUseCase;
    private final AddChatterUseCase addChatterUseCase;

    public ChatApi(final SendMessageUseCase sendMessageUseCase,
                   final AddChatterUseCase addChatterUseCase) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.addChatterUseCase = addChatterUseCase;
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return signIn;
    }
}
