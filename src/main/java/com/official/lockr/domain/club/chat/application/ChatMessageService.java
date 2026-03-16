package com.official.lockr.domain.club.chat.application;

import com.official.lockr.domain.club.chat.application.command.DeleteMessageCommand;
import com.official.lockr.domain.club.chat.application.command.LeaveChatRoomCommand;
import com.official.lockr.domain.club.chat.application.command.PinMessageCommand;
import com.official.lockr.domain.club.chat.application.command.UnpinMessageCommand;
import com.official.lockr.domain.club.chat.application.command.UpdateMessageCommand;
import com.official.lockr.domain.club.chat.application.usecase.DeleteMessageUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetPinnedMessagesUseCase;
import com.official.lockr.domain.club.chat.application.usecase.LeaveChatRoomUseCase;
import com.official.lockr.domain.club.chat.application.usecase.PinMessageUseCase;
import com.official.lockr.domain.club.chat.application.usecase.UnpinMessageUseCase;
import com.official.lockr.domain.club.chat.application.usecase.UpdateMessageUseCase;
import com.official.lockr.domain.club.chat.domain.*;
import com.official.lockr.domain.club.chat.domain.event.ChatSseEvent;
import com.official.lockr.domain.club.chat.infrastructure.sse.SseChatEventPublisher;
import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.ClubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;

@Service
public class ChatMessageService implements DeleteMessageUseCase, PinMessageUseCase, UnpinMessageUseCase, GetPinnedMessagesUseCase, UpdateMessageUseCase, LeaveChatRoomUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageService.class);

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PinnedMessageRepository pinnedMessageRepository;
    private final ClubRepository clubRepository;
    private final SseChatEventPublisher sseChatEventPublisher;

    public ChatMessageService(
            @Qualifier("chatRepositoryAdapter") final ChatRepository chatRepository,
            final ChatRoomRepository chatRoomRepository,
            final PinnedMessageRepository pinnedMessageRepository,
            final ClubRepository clubRepository,
            final SseChatEventPublisher sseChatEventPublisher) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.pinnedMessageRepository = pinnedMessageRepository;
        this.clubRepository = clubRepository;
        this.sseChatEventPublisher = sseChatEventPublisher;
    }

    @Override
    public void delete(final DeleteMessageCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.requesterId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.requesterId());
        }

        final Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + command.chatId()));

        // 권한 체크: 본인 메시지이거나 회장/매니저
        if (!chat.getSenderId().equals(command.requesterId())) {
            final Club club = clubRepository.findById(command.clubId());
            if (isNull(club)) {
                throw new IllegalArgumentException("Club not found: " + command.clubId());
            }
            boolean isPresidentOrManager = club.isPresidency(command.requesterId())
                    || club.isStaff(command.requesterId());
            if (!isPresidentOrManager) {
                throw new IllegalArgumentException("No permission to delete this message");
            }
        }

        chatRepository.softDelete(command.chatId());

        // 고정된 메시지였다면 고정도 해제
        pinnedMessageRepository.deleteByChatRoomIdAndChatId(command.chatRoomId(), command.chatId());

        // SSE 브로드캐스트
        try {
            sseChatEventPublisher.publish(
                    ChatSseEvent.messageDeleted(command.chatRoomId(), chatRoom.getClubId(), command.chatId()));
        } catch (Exception e) {
            log.error("Failed to publish SSE for message deletion. chatId={}", command.chatId(), e);
        }
    }

    @Override
    public PinnedMessage pin(final PinMessageCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.requesterId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.requesterId());
        }

        // 권한 체크: 회장/매니저만
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalArgumentException("Club not found: " + command.clubId());
        }
        boolean isPresidentOrManager = club.isPresidency(command.requesterId())
                || club.isStaff(command.requesterId());
        if (!isPresidentOrManager) {
            throw new IllegalArgumentException("No permission to pin messages. Only president/manager allowed.");
        }

        final Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + command.chatId()));

        if (chat.isDeleted()) {
            throw new IllegalArgumentException("Cannot pin a deleted message");
        }

        // 이미 고정된 메시지인지 확인
        if (pinnedMessageRepository.findByChatRoomIdAndChatId(command.chatRoomId(), command.chatId()).isPresent()) {
            throw new IllegalArgumentException("Message is already pinned");
        }

        final PinnedMessage pinnedMessage = new PinnedMessage(
                generateUlid(),
                command.chatRoomId(),
                command.chatId(),
                command.requesterId(),
                LocalDateTime.now()
        );
        pinnedMessageRepository.save(pinnedMessage);

        // SSE 브로드캐스트
        try {
            sseChatEventPublisher.publish(
                    ChatSseEvent.messagePinned(command.chatRoomId(), chatRoom.getClubId(), chat));
        } catch (Exception e) {
            log.error("Failed to publish SSE for message pin. chatId={}", command.chatId(), e);
        }

        return pinnedMessage;
    }

    @Override
    public void unpin(final UnpinMessageCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.requesterId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.requesterId());
        }

        // 권한 체크: 회장/매니저만
        final Club club = clubRepository.findById(command.clubId());
        if (isNull(club)) {
            throw new IllegalArgumentException("Club not found: " + command.clubId());
        }
        boolean isPresidentOrManager = club.isPresidency(command.requesterId())
                || club.isStaff(command.requesterId());
        if (!isPresidentOrManager) {
            throw new IllegalArgumentException("No permission to unpin messages. Only president/manager allowed.");
        }

        pinnedMessageRepository.deleteByChatRoomIdAndChatId(command.chatRoomId(), command.chatId());

        // SSE 브로드캐스트
        try {
            sseChatEventPublisher.publish(
                    ChatSseEvent.messageUnpinned(command.chatRoomId(), chatRoom.getClubId(), command.chatId()));
        } catch (Exception e) {
            log.error("Failed to publish SSE for message unpin. chatId={}", command.chatId(), e);
        }
    }

    @Override
    public List<PinnedMessage> getPinnedMessages(final String chatRoomId, final String userId) {
        final ChatRoom chatRoom = findChatRoom(chatRoomId);
        if (!chatRoom.hasMember(userId)) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + userId);
        }
        return pinnedMessageRepository.findAllByChatRoomId(chatRoomId);
    }

    @Override
    public Chat update(final UpdateMessageCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.requesterId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.requesterId());
        }

        final Chat chat = chatRepository.findById(command.chatId())
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + command.chatId()));

        if (chat.isDeleted()) {
            throw new IllegalArgumentException("Cannot update a deleted message");
        }

        if (!chat.getSenderId().equals(command.requesterId())) {
            throw new IllegalArgumentException("Only the message author can update the message");
        }

        chatRepository.updateContent(command.chatId(), command.content());

        final Chat updatedChat = new Chat(
                chat.getId(), chat.getChatRoomId(), chat.getSenderId(), command.content(),
                chat.getRepliedToId(), chat.getQuotedSenderName(), chat.getQuotedContent(), chat.getCreatedAt()
        );

        try {
            sseChatEventPublisher.publish(
                    ChatSseEvent.messageUpdated(command.chatRoomId(), chatRoom.getClubId(), updatedChat));
        } catch (Exception e) {
            log.error("Failed to publish SSE for message update. chatId={}", command.chatId(), e);
        }

        return updatedChat;
    }

    @Override
    public void leave(final LeaveChatRoomCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        if (!chatRoom.hasMember(command.userId())) {
            throw new IllegalArgumentException("User is not a member of the chat room: " + command.userId());
        }

        chatRoom.removeChatter(command.userId());
        chatRoomRepository.save(chatRoom);

        try {
            sseChatEventPublisher.publish(
                    ChatSseEvent.chatterLeft(command.chatRoomId(), chatRoom.getClubId(), command.userId()));
        } catch (Exception e) {
            log.error("Failed to publish SSE for chatter leave. userId={}", command.userId(), e);
        }
    }

    private ChatRoom findChatRoom(final String chatRoomId) {
        final ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);
        if (isNull(chatRoom)) {
            throw new IllegalArgumentException("ChatRoom not found: " + chatRoomId);
        }
        return chatRoom;
    }
}
