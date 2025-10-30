# 채팅 기능 가이드

## 개요
클럽 내 실시간 채팅 기능을 제공합니다. SSE(Server-Sent Events)를 통한 실시간 메시지 푸시를 지원하며, 다양한 저장소 백엔드를 선택할 수 있습니다.

## 기능

### 1. 채팅방 관리
- 채팅방 생성
- 채팅방 목록 조회
- 참여자 추가/제거

### 2. 메시지 관리
- 메시지 전송
- 메시지 히스토리 조회
- 실시간 메시지 수신 (SSE)

### 3. 실시간 업데이트 (SSE)
- 새로운 메시지 실시간 수신
- 채팅방 업데이트 실시간 알림
- 클럽 전체 채팅 이벤트 구독

## API 엔드포인트

### REST API

#### 채팅방 관리
```
POST   /api/v1/clubs/{clubId}/chats/rooms                       - 채팅방 생성
GET    /api/v1/clubs/{clubId}/chats/rooms                       - 채팅방 목록 조회
POST   /api/v1/clubs/{clubId}/chats/rooms/{chatRoomId}/chatters - 참여자 추가
```

#### 메시지 관리
```
POST   /api/v1/clubs/{clubId}/chats/rooms/{chatRoomId}/messages - 메시지 전송
GET    /api/v1/clubs/{clubId}/chats/rooms/{chatRoomId}/messages - 메시지 조회
```

#### SSE 스트림
```
GET    /api/v1/clubs/{clubId}/chats/stream                      - 클럽 전체 채팅 이벤트 구독
GET    /api/v1/clubs/{clubId}/chats/rooms/{chatRoomId}/stream   - 특정 채팅방 이벤트 구독
```

### SSE 이벤트 타입
- `NEW_MESSAGE`: 새로운 메시지
- `ROOM_UPDATED`: 채팅방 업데이트 (생성, 참여자 변경 등)
- `CHATTER_JOINED`: 참여자 입장
- `CHATTER_LEFT`: 참여자 퇴장

## 저장소 선택

채팅 메시지는 3가지 저장소 중 선택하여 사용할 수 있습니다:

### 1. JOOQ (RDB - MySQL) - 기본값
기본 설정으로 JOOQ 기반 RDB를 사용합니다.

**설정 불필요** (기본값)

**테이블 구조:**
- `chat_rooms`: 채팅방 정보
- `chatters`: 채팅 참여자
- `chats`: 채팅 메시지

### 2. MongoDB (NoSQL)
대용량 메시지 처리에 적합합니다.

**의존성 추가 (build.gradle):**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
```

**application.yml 설정:**
```yaml
chat:
  repository:
    type: mongodb

spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/lockr
      database: lockr
```

### 3. Redis (In-Memory)
초고속 메시지 처리가 필요한 경우 적합합니다.
- Sorted Set을 사용하여 시간 순 정렬 보장
- 메시지는 JSON으로 직렬화

**의존성 추가 (build.gradle):**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

**application.yml 설정:**
```yaml
chat:
  repository:
    type: redis

spring:
  redis:
    host: localhost
    port: 6379
```

**주의사항:**
- Redis는 인메모리 저장소이므로 영구 저장이 필요한 경우 RDB Persistence 설정 필요
- TTL 설정으로 오래된 메시지 자동 삭제 가능

## 프론트엔드 연동 예시

### SSE 연결 (JavaScript)
```javascript
// 특정 채팅방 구독
const eventSource = new EventSource('/api/v1/clubs/{clubId}/chats/rooms/{chatRoomId}/stream');

eventSource.addEventListener('NEW_MESSAGE', (event) => {
  const data = JSON.parse(event.data);
  console.log('새 메시지:', data.chat);
  // UI 업데이트 로직
});

eventSource.addEventListener('ROOM_UPDATED', (event) => {
  const data = JSON.parse(event.data);
  console.log('채팅방 업데이트:', data.chatRoomId);
  // 채팅방 목록 새로고침
});

eventSource.addEventListener('connected', (event) => {
  console.log('연결 성공:', event.data);
});

// 연결 종료
eventSource.close();
```

### 메시지 전송
```javascript
fetch('/api/v1/clubs/{clubId}/chats/rooms/{chatRoomId}/messages', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    message: '안녕하세요!',
    senderNickname: '홍길동'
  })
});
```

## 성능 최적화 권장사항

### 1. 대용량 트래픽
- **Redis 사용 권장**
- Redis Cluster 구성으로 수평 확장
- 메시지 TTL 설정 (예: 30일)

### 2. 영구 저장 중요
- **RDB (JOOQ) 또는 MongoDB 사용**
- 정기적인 백업 설정
- 오래된 메시지 아카이빙 전략

### 3. 하이브리드 아키텍처
```
[클라이언트]
    ↓
[SSE] ← [Redis (실시간 메시지)] → [배치 작업]
    ↓                                    ↓
[REST API]                        [MySQL (영구 저장)]
```

- 실시간 메시지: Redis
- 영구 저장: MySQL/MongoDB (비동기 저장)
- SSE로 실시간 푸시

## 확장 가능한 설계

현재 Repository 인터페이스를 구현하면 다른 저장소도 쉽게 추가 가능:
- Cassandra
- DynamoDB
- Elasticsearch

```java
@Repository
@ConditionalOnProperty(name = "chat.repository.type", havingValue = "cassandra")
public class CassandraChatRepository implements ChatRepository {
    // 구현
}
```

## 보안 고려사항

1. **인증/인가**: HttpSession으로 사용자 인증 확인
2. **채팅방 권한**: 클럽 멤버만 채팅 가능
3. **메시지 길이 제한**: 최대 1000자 (DB 스키마)
4. **XSS 방지**: 프론트엔드에서 메시지 이스케이프 처리 필요

## 모니터링

### 주요 지표
- SSE 연결 수 (동시 접속자)
- 메시지 전송 속도 (TPS)
- 저장소 응답 시간
- SSE 연결 실패/타임아웃

### 로깅
```java
// SseChatEventPublisher에서 연결 정보 로깅
logger.info("SSE connected - clubId: {}, active connections: {}", clubId, clubEmitters.get(clubId).size());
```
