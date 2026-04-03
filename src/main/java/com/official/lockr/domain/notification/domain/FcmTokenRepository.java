package com.official.lockr.domain.notification.domain;

import java.util.List;

public interface FcmTokenRepository {
    FcmToken save(FcmToken fcmToken);
    List<FcmToken> findByUserId(String userId);
    List<String> findTokensByUserIds(List<String> userIds);
    void deleteByUserId(String userId);
    void deleteByToken(String token);
}
