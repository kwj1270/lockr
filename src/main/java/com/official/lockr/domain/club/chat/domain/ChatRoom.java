package com.official.lockr.domain.club.chat.domain;

import com.official.lockr.domain.club.chat.domain.event.CreatedChatRoomEvent;
import com.official.lockr.domain.club.chat.domain.event.RemovedChatterEvent;
import com.official.lockr.global.ddd.AggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatRoom extends AggregateRoot {

    private final String id;
    private final String clubId;
    private final String name;
    private final List<Chatter> chatters;
    private final Set<String> userIdCache;  // O(1) 조회를 위한 캐시
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public static ChatRoom init(final String id, final String clubId, final String name, final String defaultChatterUserId) {
        final ChatRoom chatRoom = new ChatRoom(id, clubId, name, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null);
        chatRoom.addEvent(new CreatedChatRoomEvent(chatRoom.id, chatRoom.clubId));
        return chatRoom;
    }

    public ChatRoom(final String id, final String clubId, final String name, final List<Chatter> chatters,
                    final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt) {
        this.id = id;
        this.clubId = clubId;
        this.name = name;
        this.chatters = chatters;
        // userId 캐시 초기화 (O(1) 조회 성능)
        this.userIdCache = new HashSet<>();
        for (Chatter chatter : chatters) {
            this.userIdCache.add(chatter.getUserId());
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public void addChatter(final String userId) {
        // O(1) 중복 체크
        if (userIdCache.contains(userId)) {
            throw new IllegalArgumentException("User already in chat room: " + userId);
        }
        final Chatter chatter = new Chatter(userId);
        chatters.add(chatter);
        userIdCache.add(userId);
    }

    public void removeChatter(final String userId) {
        chatters.removeIf(chatter -> chatter.isSame(userId));
        userIdCache.remove(userId);
        addEvent(new RemovedChatterEvent(this.id, this.clubId, userId));
    }

    public boolean hasMember(final String userId) {
        // O(1) 조회
        return userIdCache.contains(userId);
    }

    public String getId() {
        return id;
    }

    public String getClubId() {
        return clubId;
    }

    public String getName() {
        return name;
    }

    public List<Chatter> getChatters() {
        return List.copyOf(chatters);  // 불변 복사본 반환으로 캡슐화 보장
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

}
