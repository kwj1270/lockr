package com.official.lockr.domain.club.chat.infrastructure.sse;

import com.official.lockr.domain.club.chat.domain.event.ChatSseEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SseChatEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SseChatEventPublisher.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    // chatRoomId -> List<SseEmitter>
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> chatRoomEmitters = new ConcurrentHashMap<>();
    // clubId -> List<SseEmitter>
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> clubEmitters = new ConcurrentHashMap<>();

    // 모니터링
    private final AtomicInteger clubConnectionCount = new AtomicInteger(0);
    private final AtomicInteger chatRoomConnectionCount = new AtomicInteger(0);
    private final Counter clubConnectionOpened;
    private final Counter clubConnectionClosedCompleted;
    private final Counter clubConnectionClosedTimeout;
    private final Counter clubConnectionClosedError;
    private final Counter chatRoomConnectionOpened;
    private final Counter chatRoomConnectionClosedCompleted;
    private final Counter chatRoomConnectionClosedTimeout;
    private final Counter chatRoomConnectionClosedError;
    private final Counter clubMessageSent;
    private final Counter clubMessageFailed;
    private final Counter chatRoomMessageSent;
    private final Counter chatRoomMessageFailed;

    public SseChatEventPublisher(MeterRegistry meterRegistry) {
        Gauge.builder("sse.connections.active", clubConnectionCount, AtomicInteger::get)
                .tag("type", "club")
                .description("Active SSE connections for club")
                .register(meterRegistry);
        Gauge.builder("sse.connections.active", chatRoomConnectionCount, AtomicInteger::get)
                .tag("type", "chat_room")
                .description("Active SSE connections for chat room")
                .register(meterRegistry);

        this.clubConnectionOpened = Counter.builder("sse.connections.opened")
                .tag("type", "club")
                .register(meterRegistry);
        this.clubConnectionClosedCompleted = Counter.builder("sse.connections.closed")
                .tag("type", "club").tag("reason", "completed")
                .register(meterRegistry);
        this.clubConnectionClosedTimeout = Counter.builder("sse.connections.closed")
                .tag("type", "club").tag("reason", "timeout")
                .register(meterRegistry);
        this.clubConnectionClosedError = Counter.builder("sse.connections.closed")
                .tag("type", "club").tag("reason", "error")
                .register(meterRegistry);

        this.chatRoomConnectionOpened = Counter.builder("sse.connections.opened")
                .tag("type", "chat_room")
                .register(meterRegistry);
        this.chatRoomConnectionClosedCompleted = Counter.builder("sse.connections.closed")
                .tag("type", "chat_room").tag("reason", "completed")
                .register(meterRegistry);
        this.chatRoomConnectionClosedTimeout = Counter.builder("sse.connections.closed")
                .tag("type", "chat_room").tag("reason", "timeout")
                .register(meterRegistry);
        this.chatRoomConnectionClosedError = Counter.builder("sse.connections.closed")
                .tag("type", "chat_room").tag("reason", "error")
                .register(meterRegistry);

        this.clubMessageSent = Counter.builder("sse.messages.sent")
                .tag("type", "club")
                .register(meterRegistry);
        this.clubMessageFailed = Counter.builder("sse.messages.failed")
                .tag("type", "club")
                .register(meterRegistry);
        this.chatRoomMessageSent = Counter.builder("sse.messages.sent")
                .tag("type", "chat_room")
                .register(meterRegistry);
        this.chatRoomMessageFailed = Counter.builder("sse.messages.failed")
                .tag("type", "chat_room")
                .register(meterRegistry);
    }

    public void publish(final ChatSseEvent event) {
        // 특정 채팅방 구독자들에게 전송
        sendToRoom(event.chatRoomId(), event);

        // 전체 클럽 구독자들에게 전송 (채팅방 목록 갱신용)
        sendToClub(event.clubId(), event);
    }

    public SseEmitter subscribeToClub(final String clubId) {
        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        clubEmitters.computeIfAbsent(clubId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        clubConnectionCount.incrementAndGet();
        clubConnectionOpened.increment();
        log.debug("SSE club 연결 시작: clubId={}, activeConnections={}", clubId, clubConnectionCount.get());

        emitter.onCompletion(() -> {
            removeClubEmitter(clubId, emitter);
            clubConnectionCount.decrementAndGet();
            clubConnectionClosedCompleted.increment();
            log.debug("SSE club 연결 완료(onCompletion): clubId={}", clubId);
        });
        emitter.onTimeout(() -> {
            clubConnectionClosedTimeout.increment();
            log.debug("SSE club 연결 타임아웃(onTimeout): clubId={}", clubId);
        });
        emitter.onError(e -> {
            clubConnectionClosedError.increment();
            log.warn("SSE club 연결 에러(onError): clubId={}, error={}", clubId, e.getMessage());
        });

        // 초기 연결 확인 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to club: " + clubId));
            log.debug("SSE club 초기 연결 이벤트 전송 성공: clubId={}", clubId);
        } catch (IOException e) {
            log.warn("SSE club 초기 연결 이벤트 전송 실패: clubId={}, error={}", clubId, e.getMessage());
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public SseEmitter subscribeToChatRoom(final String chatRoomId) {
        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        chatRoomEmitters.computeIfAbsent(chatRoomId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        chatRoomConnectionCount.incrementAndGet();
        chatRoomConnectionOpened.increment();
        log.debug("SSE chatRoom 연결 시작: chatRoomId={}, activeConnections={}", chatRoomId, chatRoomConnectionCount.get());

        emitter.onCompletion(() -> {
            removeChatRoomEmitter(chatRoomId, emitter);
            chatRoomConnectionCount.decrementAndGet();
            chatRoomConnectionClosedCompleted.increment();
            log.debug("SSE chatRoom 연결 완료(onCompletion): chatRoomId={}", chatRoomId);
        });
        emitter.onTimeout(() -> {
            chatRoomConnectionClosedTimeout.increment();
            log.debug("SSE chatRoom 연결 타임아웃(onTimeout): chatRoomId={}", chatRoomId);
        });
        emitter.onError(e -> {
            chatRoomConnectionClosedError.increment();
            log.warn("SSE chatRoom 연결 에러(onError): chatRoomId={}, error={}", chatRoomId, e.getMessage());
        });

        // 초기 연결 확인 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to chat room: " + chatRoomId));
            log.debug("SSE chatRoom 초기 연결 이벤트 전송 성공: chatRoomId={}", chatRoomId);
        } catch (IOException e) {
            log.warn("SSE chatRoom 초기 연결 이벤트 전송 실패: chatRoomId={}, error={}", chatRoomId, e.getMessage());
            emitter.completeWithError(e);
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
                chatRoomMessageSent.increment();
            } catch (IOException e) {
                chatRoomMessageFailed.increment();
                emitter.completeWithError(e);
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
                clubMessageSent.increment();
            } catch (IOException e) {
                clubMessageFailed.increment();
                emitter.completeWithError(e);
            }
        });
    }

    private void removeClubEmitter(final String clubId, final SseEmitter emitter) {
        clubEmitters.computeIfPresent(clubId, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    private void removeChatRoomEmitter(final String chatRoomId, final SseEmitter emitter) {
        chatRoomEmitters.computeIfPresent(chatRoomId, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        clubEmitters.forEach((clubId, emitters) ->
                emitters.forEach(this::sendHeartbeatToEmitter)
        );

        chatRoomEmitters.forEach((chatRoomId, emitters) ->
                emitters.forEach(this::sendHeartbeatToEmitter)
        );

        // 빈 리스트 키 정리 (stale entry 방지)
        clubEmitters.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        chatRoomEmitters.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private void sendHeartbeatToEmitter(final SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("heartbeat")
                    .data("ping"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
