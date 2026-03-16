package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.club.chat.application.command.AddChatterCommand;
import com.official.lockr.domain.club.chat.application.command.CreateChatRoomCommand;
import com.official.lockr.domain.club.chat.application.command.RemoveChatterCommand;
import com.official.lockr.domain.club.chat.application.usecase.AddChatterUseCase;
import com.official.lockr.domain.club.chat.application.usecase.CreateChatRoomUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetChatRoomsUseCase;
import com.official.lockr.domain.club.chat.application.usecase.RemoveChatterUseCase;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import com.official.lockr.domain.club.club.domain.event.RemovedClubMemberEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class ChatConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatConsumer.class);

    private final CreateChatRoomUseCase createChatRoomUseCase;
    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final AddChatterUseCase addChatterUseCase;
    private final RemoveChatterUseCase removeChatterUseCase;
    private final RetryTemplate addMemberRetryTemplate;

    public ChatConsumer(final CreateChatRoomUseCase createChatRoomUseCase,
                        final GetChatRoomsUseCase getChatRoomsUseCase,
                        final AddChatterUseCase addChatterUseCase,
                        final RemoveChatterUseCase removeChatterUseCase
    ) {
        this.createChatRoomUseCase = createChatRoomUseCase;
        this.getChatRoomsUseCase = getChatRoomsUseCase;
        this.addChatterUseCase = addChatterUseCase;
        this.removeChatterUseCase = removeChatterUseCase;
        this.addMemberRetryTemplate = RetryTemplate.builder()
                .maxAttempts(10)
                .exponentialBackoff(1000, 1.5, 5000)
                .retryOn(IllegalStateException.class)
                .build();
    }

    @TransactionalEventListener
    public void create(final FoundClubEvent event) {
        try {
            log.info("Creating chat room for club: {}", event.id());
            createChatRoomUseCase.create(new CreateChatRoomCommand(event.id(), event.name(), event.foundUserId()));
            log.info("Chat room created successfully for club: {}", event.id());
        } catch (Exception e) {
            log.error("Failed to create chat room for club: {}", event.id(), e);
        }
    }

    @TransactionalEventListener
    public void addMember(final AddedClubMemberEvent event) {
        log.debug("Received AddedMemberEvent for club: {}, user: {}", event.clubId(), event.userId());
        try {
            addMemberRetryTemplate.execute(ctx -> {
                doAddMember(event);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to add member to chat room after all retries. club: {}, user: {}",
                    event.clubId(), event.userId(), e);
        }
    }

    @TransactionalEventListener
    public void removeMember(final RemovedClubMemberEvent event) {
        log.debug("Received RemovedClubMemberEvent for club: {}, user: {}", event.clubId(), event.userId());
        try {
            final List<ChatRoom> chatRooms = getChatRoomsUseCase.getChatRooms(event.clubId(), event.userId());
            if (chatRooms.isEmpty()) {
                log.warn("ChatRoom not found for club: {}. Skipping chatter removal.", event.clubId());
                return;
            }
            final ChatRoom chatRoom = chatRooms.getFirst();
            removeChatterUseCase.removeChatter(new RemoveChatterCommand(
                    chatRoom.getClubId(),
                    chatRoom.getId(),
                    event.userId()
            ));
            log.info("Member removed from chat room successfully. club: {}, user: {}, chatRoom: {}",
                    event.clubId(), event.userId(), chatRoom.getId());
        } catch (Exception e) {
            log.error("Failed to remove member from chat room. club: {}, user: {}",
                    event.clubId(), event.userId(), e);
        }
    }

    private void doAddMember(final AddedClubMemberEvent event) {
        log.debug("Attempting to add member to chat room. club: {}, user: {}", event.clubId(), event.userId());

        final List<ChatRoom> chatRooms = getChatRoomsUseCase.getChatRooms(event.clubId(), event.userId());
        if (chatRooms.isEmpty()) {
            log.warn("ChatRoom not found yet for club: {}. Will retry...", event.clubId());
            throw new IllegalStateException("ChatRoom not created yet for club: " + event.clubId());
        }
        final ChatRoom chatRoom = chatRooms.getFirst();
        addChatterUseCase.addChatter(new AddChatterCommand(
                chatRoom.getClubId(),
                chatRoom.getId(),
                event.userId()
        ));

        log.info("Member added to chat room successfully. club: {}, user: {}, chatRoom: {}",
                event.clubId(), event.userId(), chatRoom.getId());
    }
}
