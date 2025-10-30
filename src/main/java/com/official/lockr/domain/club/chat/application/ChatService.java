package com.official.lockr.domain.club.chat.application;

import com.official.lockr.domain.club.chat.application.dto.SendMessageCommand;
import com.official.lockr.domain.club.chat.application.usecase.GetMessagesUseCase;
import com.official.lockr.domain.club.chat.application.usecase.SendMessageUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRepository;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ChatService implements SendMessageUseCase, GetMessagesUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;

    public ChatService(final ChatRoomRepository chatRoomRepository,
                       @Qualifier("chatRepositoryAdapter") final ChatRepository chatRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    public Chat send(final SendMessageCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.senderId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.senderId());
        }
        final Chat chat = Chat.init(
                generateUlid(),
                command.chatRoomId(),
                command.senderId(),
                command.senderNickname(),
                command.message()
        );
        return chatRepository.save(chat);
    }

    @Override
    public List<Chat> getMessages(final String chatRoomId, final String userId, final String lastChatId, int limit) {
        final ChatRoom chatRoom = findChatRoom(chatRoomId);
        if (!chatRoom.hasMember(userId)) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + userId);
        }
        return chatRepository.findAllByChatRoomId(chatRoom.getId(), lastChatId, limit);
    }

    private ChatRoom findChatRoom(final String chatRoomId) {
        final ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);
        if (isNull(chatRoom)) {
            throw new IllegalArgumentException("ChatRoom not found: " + chatRoomId);
        }
        return chatRoom;
    }

}
