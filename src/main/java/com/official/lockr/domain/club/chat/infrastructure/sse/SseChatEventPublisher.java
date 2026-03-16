package com.official.lockr.domain.club.chat.infrastructure.sse;

import com.official.lockr.domain.club.chat.domain.event.ChatSseEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseChatEventPublisher {

    // chatRoomId -> List<SseEmitter>
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> chatRoomEmitters = new ConcurrentHashMap<>();
    // clubId -> List<SseEmitter>
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> clubEmitters = new ConcurrentHashMap<>();

    public void publish(final ChatSseEvent event) {
        // 특정 채팅방 구독자들에게 전송
        sendToRoom(event.chatRoomId(), event);

        // 전체 클럽 구독자들에게 전송 (채팅방 목록 갱신용)
        sendToClub(event.clubId(), event);
    }

    public SseEmitter subscribeToClub(final String clubId) {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        clubEmitters.computeIfAbsent(clubId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeClubEmitter(clubId, emitter));
        emitter.onTimeout(() -> removeClubEmitter(clubId, emitter));
        emitter.onError(e -> removeClubEmitter(clubId, emitter));

        // 초기 연결 확인 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to club: " + clubId));
        } catch (IOException e) {
            removeClubEmitter(clubId, emitter);
        }

        return emitter;
    }

    public SseEmitter subscribeToChatRoom(final String chatRoomId) {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        chatRoomEmitters.computeIfAbsent(chatRoomId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeChatRoomEmitter(chatRoomId, emitter));
        emitter.onTimeout(() -> removeChatRoomEmitter(chatRoomId, emitter));
        emitter.onError(e -> removeChatRoomEmitter(chatRoomId, emitter));

        // 초기 연결 확인 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to chat room: " + chatRoomId));
        } catch (IOException e) {
            removeChatRoomEmitter(chatRoomId, emitter);
        }

        return emitter;
    }

    private void sendToRoom(final String chatRoomId, final ChatSseEvent event) {
        final CopyOnWriteArrayList<SseEmitter> emitters = chatRoomEmitters.get(chatRoomId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.type().name())
                        .data(event));
            } catch (IOException e) {
                removeChatRoomEmitter(chatRoomId, emitter);
            }
        });
    }

    private void sendToClub(final String clubId, final ChatSseEvent event) {
        final CopyOnWriteArrayList<SseEmitter> emitters = clubEmitters.get(clubId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.type().name())
                        .data(event));
            } catch (IOException e) {
                removeClubEmitter(clubId, emitter);
            }
        });
    }

    private void removeClubEmitter(final String clubId, final SseEmitter emitter) {
        final CopyOnWriteArrayList<SseEmitter> emitters = clubEmitters.get(clubId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                clubEmitters.remove(clubId);
            }
        }
    }

    private void removeChatRoomEmitter(final String chatRoomId, final SseEmitter emitter) {
        final CopyOnWriteArrayList<SseEmitter> emitters = chatRoomEmitters.get(chatRoomId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                chatRoomEmitters.remove(chatRoomId);
            }
        }
    }
}
