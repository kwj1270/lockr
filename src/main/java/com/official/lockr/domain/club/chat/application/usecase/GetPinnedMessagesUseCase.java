package com.official.lockr.domain.club.chat.application.usecase;

import com.official.lockr.domain.club.chat.domain.PinnedMessage;

import java.util.List;

public interface GetPinnedMessagesUseCase {
    List<PinnedMessage> getPinnedMessages(String chatRoomId, String userId);
}
