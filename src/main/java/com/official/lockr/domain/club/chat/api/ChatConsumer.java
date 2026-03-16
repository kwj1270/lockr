package com.official.lockr.domain.club.chat.api;

import com.official.lockr.domain.club.chat.application.dto.AddChatterCommand;
import com.official.lockr.domain.club.chat.application.dto.CreateChatRoomCommand;
import com.official.lockr.domain.club.chat.application.usecase.AddChatterUseCase;
import com.official.lockr.domain.club.chat.application.usecase.CreateChatRoomUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetChatRoomsUseCase;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.club.domain.event.AddedClubMemberEvent;
import com.official.lockr.domain.club.club.domain.event.FoundClubEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatConsumer.class);

    private final CreateChatRoomUseCase createChatRoomUseCase;
    private final GetChatRoomsUseCase getChatRoomsUseCase;
    private final AddChatterUseCase addChatterUseCase;

    public ChatConsumer(final CreateChatRoomUseCase createChatRoomUseCase,
                        final GetChatRoomsUseCase getChatRoomsUseCase,
                        final AddChatterUseCase addChatterUseCase
    ) {
        this.createChatRoomUseCase = createChatRoomUseCase;
        this.getChatRoomsUseCase = getChatRoomsUseCase;
        this.addChatterUseCase = addChatterUseCase;
    }

    @EventListener
    public void create(final FoundClubEvent event) {
        log.info("Creating chat room for club: {}", event.id());
        createChatRoomUseCase.create(new CreateChatRoomCommand(event.id(), event.name(), event.foundUserId()));
        log.info("Chat room created successfully for club: {}", event.id());
    }

    @EventListener
    public void addMember(final AddedClubMemberEvent event) {
        log.debug("Received AddedMemberEvent for club: {}, user: {}", event.clubId(), event.userId());
        try {
            addMemberWithRetry(event);
        } catch (Exception e) {
            log.error("Failed to add member to chat room after retries. club: {}, user: {}", event.clubId(), event.userId(), e);
        }
    }

    @Retryable(
            retryFor = {IllegalStateException.class},
            maxAttempts = 10,
            backoff = @Backoff(delay = 1000, multiplier = 1.5, maxDelay = 5000)
    )
    public void addMemberWithRetry(final AddedClubMemberEvent event) {
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

        log.info("Member added to chat room successfully. club: {}, user: {}, chatRoom: {}", event.clubId(), event.userId(), chatRoom.getId());
    }

    @Recover
    public void recoverAddMember(final IllegalStateException e, final AddedClubMemberEvent event) {
        log.error("Failed to add member to chat room after all retries. " + "ChatRoom may not have been created. club: {}, user: {}", event.clubId(), event.userId(), e);
    }
}
