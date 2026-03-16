package com.official.lockr.domain.club.chat.application;

import com.official.lockr.domain.club.chat.application.command.AddChatterCommand;
import com.official.lockr.domain.club.chat.application.command.CreateChatRoomCommand;
import com.official.lockr.domain.club.chat.application.command.RemoveChatterCommand;
import com.official.lockr.domain.club.chat.application.usecase.AddChatterUseCase;
import com.official.lockr.domain.club.chat.application.usecase.CreateChatRoomUseCase;
import com.official.lockr.domain.club.chat.application.usecase.GetChatRoomsUseCase;
import com.official.lockr.domain.club.chat.application.usecase.RemoveChatterUseCase;
import com.official.lockr.domain.club.chat.domain.ChatRoom;
import com.official.lockr.domain.club.chat.domain.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.official.lockr.global.util.UlidUtils.generateUlid;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class ChatRoomService implements CreateChatRoomUseCase, AddChatterUseCase, RemoveChatterUseCase, GetChatRoomsUseCase {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(final ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Override
    public ChatRoom create(final CreateChatRoomCommand command) {
        final ChatRoom existedClub = chatRoomRepository.findByClubId(command.clubId());
        if (nonNull(existedClub)) {
            return existedClub;
        }
        final ChatRoom chatRoom = ChatRoom.init(generateUlid(), command.clubId(), command.name(), command.defaultChatterUserId());
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoom addChatter(final AddChatterCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        chatRoom.addChatter(command.userId());
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public ChatRoom removeChatter(final RemoveChatterCommand command) {
        final ChatRoom chatRoom = findChatRoom(command.chatRoomId());
        chatRoom.removeChatter(command.userId());
        return chatRoomRepository.save(chatRoom);
    }

    @Override
    public List<ChatRoom> getChatRooms(final String clubId, final String userId) {
        return chatRoomRepository.findAllByClubId(clubId);
    }

    private ChatRoom findChatRoom(final String chatRoomId) {
        final ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId);
        if (isNull(chatRoom)) {
            throw new IllegalArgumentException("ChatRoom not found: " + chatRoomId);
        }
        return chatRoom;
    }
}
