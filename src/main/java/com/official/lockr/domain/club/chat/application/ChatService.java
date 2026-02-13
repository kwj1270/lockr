package com.official.lockr.domain.club.chat.application;

import com.official.lockr.domain.club.chat.application.command.SendMessageCommand;
import com.official.lockr.domain.club.chat.application.usecase.GetMessagesUseCase;
import com.official.lockr.domain.club.chat.application.usecase.SendMessageUseCase;
import com.official.lockr.domain.club.chat.domain.Chat;
import com.official.lockr.domain.club.chat.domain.ChatRepository;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import com.official.lockr.domain.users.domain.Users;
import com.official.lockr.domain.users.domain.UsersRepository;
import com.official.lockr.global.util.ChatRateLimiter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.MessageSanitizer.sanitize;
import static com.official.lockr.global.util.MessageSanitizer.sanitizeQuotedContent;
import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ChatService implements SendMessageUseCase, GetMessagesUseCase {

    private static final int MAX_QUOTED_CONTENT_LENGTH = 100;

    private final UsersRepository usersRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRepository chatRepository;
    private final ChatRateLimiter chatRateLimiter;

    public ChatService(final UsersRepository usersRepository,
                       final ChatRoomRepository chatRoomRepository,
                       @Qualifier("chatRepositoryAdapter") final ChatRepository chatRepository,
                       final ChatRateLimiter chatRateLimiter) {
        this.usersRepository = usersRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRepository = chatRepository;
        this.chatRateLimiter = chatRateLimiter;
    }

    @Override
    public Chat send(final SendMessageCommand command) {
        chatRateLimiter.checkRateLimit(command.senderId());

        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.senderId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.senderId());
        }
        // 메시지 sanitize (XSS 방지)
        final String sanitizedMessage = sanitize(command.message());

        // 답장 정보 조회
        String quotedSenderName = null;
        String quotedContent = null;
        if (Strings.isNotBlank(command.repliedToId())) {
            final Chat repliedToChat = chatRepository.findById(command.repliedToId()).orElse(null);
            if (repliedToChat != null) {
                final Users quotedUser = usersRepository.findById(repliedToChat.getSenderId());
                quotedSenderName = quotedUser != null ? quotedUser.name() : null;
                quotedContent = sanitizeQuotedContent(truncateContent(repliedToChat.getMessage()));
            }
        }

        final Chat chat = Chat.init(
                generateUlid(),
                command.chatRoomId(),
                command.senderId(),
                sanitizedMessage,
                command.repliedToId(),
                quotedSenderName,
                quotedContent
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

    @Override
    public List<Chat> getMessagesAfter(final String chatRoomId, final String userId, final String afterChatId, int limit) {
        final ChatRoom chatRoom = findChatRoom(chatRoomId);
        if (!chatRoom.hasMember(userId)) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + userId);
        }
        return chatRepository.findAllAfterChatId(chatRoom.getId(), afterChatId, limit);
    }

    private ChatRoom findChatRoom(final String chatRoomId) {
        final ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);
        if (isNull(chatRoom)) {
            throw new IllegalArgumentException("ChatRoom not found: " + chatRoomId);
        }
        return chatRoom;
    }

    /**
     * 인용 내용을 최대 길이로 자르기
     */
    private String truncateContent(final String content) {
        if (content == null) {
            return null;
        }
        if (content.length() <= MAX_QUOTED_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_QUOTED_CONTENT_LENGTH) + "...";
    }
}
