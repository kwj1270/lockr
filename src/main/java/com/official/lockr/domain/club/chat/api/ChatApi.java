package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.chat.api.dto.AddChatterRequest;
import com.official.lockr.domain.club.chat.api.dto.SendMessageRequest;
import com.official.lockr.domain.club.chat.api.dto.UpdateMessageRequest;
import com.official.lockr.domain.club.chat.application.command.DeleteMessageCommand;
import com.official.lockr.domain.club.chat.application.command.LeaveChatRoomCommand;
import com.official.lockr.domain.club.chat.application.command.PinMessageCommand;
import com.official.lockr.domain.club.chat.application.command.UnpinMessageCommand;
import com.official.lockr.domain.club.chat.application.usecase.*;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.PinnedMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/clubs/{clubId}/chats")
@RestController
public class ChatApi {

    private final SendMessageUseCase sendMessageUseCase;
    private final AddChatterUseCase addChatterUseCase;
    private final DeleteMessageUseCase deleteMessageUseCase;
    private final PinMessageUseCase pinMessageUseCase;
    private final UnpinMessageUseCase unpinMessageUseCase;
    private final UpdateMessageUseCase updateMessageUseCase;
    private final LeaveChatRoomUseCase leaveChatRoomUseCase;

    public ChatApi(final SendMessageUseCase sendMessageUseCase,
                   final AddChatterUseCase addChatterUseCase,
                   final DeleteMessageUseCase deleteMessageUseCase,
                   final PinMessageUseCase pinMessageUseCase,
                   final UnpinMessageUseCase unpinMessageUseCase,
                   final UpdateMessageUseCase updateMessageUseCase,
                   final LeaveChatRoomUseCase leaveChatRoomUseCase) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.addChatterUseCase = addChatterUseCase;
        this.deleteMessageUseCase = deleteMessageUseCase;
        this.pinMessageUseCase = pinMessageUseCase;
        this.unpinMessageUseCase = unpinMessageUseCase;
        this.updateMessageUseCase = updateMessageUseCase;
        this.leaveChatRoomUseCase = leaveChatRoomUseCase;
    }

    @PostMapping("/rooms/{chatRoomId}/chatters")
    public ResponseEntity<ChatRoom> addChatter(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @RequestBody final AddChatterRequest request
    ) {
        final ChatRoom chatRoom = addChatterUseCase.addChatter(request.toCommand(clubId, chatRoomId));
        return ResponseEntity.ok(chatRoom);
    }

    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<Chat> sendMessage(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @RequestBody final SendMessageRequest request
    ) {
        final Chat chat = sendMessageUseCase.send(request.toCommand(clubId, chatRoomId, signInSession.userId()));
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/rooms/{chatRoomId}/messages/{chatId}/delete")
    public ResponseEntity<Void> deleteMessage(
            @RequestAttribute("signInSession") final SignInSession session,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId
    ) {
        deleteMessageUseCase.delete(new DeleteMessageCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{chatRoomId}/messages/{chatId}/pin")
    public ResponseEntity<PinnedMessage> pinMessage(
            @RequestAttribute("signInSession") final SignInSession session,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId
    ) {
        final PinnedMessage pinnedMessage = pinMessageUseCase.pin(
                new PinMessageCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.ok(pinnedMessage);
    }

    @PostMapping("/rooms/{chatRoomId}/messages/{chatId}/unpin")
    public ResponseEntity<Void> unpinMessage(
            @RequestAttribute("signInSession") final SignInSession session,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId
    ) {
        unpinMessageUseCase.unpin(new UnpinMessageCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{chatRoomId}/messages/{chatId}/update")
    public ResponseEntity<Chat> updateMessage(
            @RequestAttribute("signInSession") final SignInSession session,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId,
            @PathVariable final String chatId,
            @RequestBody final UpdateMessageRequest request
    ) {
        final Chat updatedChat = updateMessageUseCase.update(
                request.toCommand(clubId, chatRoomId, chatId, session.userId()));
        return ResponseEntity.ok(updatedChat);
    }

    @PostMapping("/rooms/{chatRoomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @RequestAttribute("signInSession") final SignInSession session,
            @PathVariable final String clubId,
            @PathVariable final String chatRoomId
    ) {
        leaveChatRoomUseCase.leave(new LeaveChatRoomCommand(clubId, chatRoomId, session.userId()));
        return ResponseEntity.noContent().build();
    }
}
