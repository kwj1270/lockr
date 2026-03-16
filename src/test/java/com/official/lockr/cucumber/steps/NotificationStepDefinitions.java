package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.notification.domain.Notification;
import com.official.lockr.domain.notification.domain.NotificationType;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationStepDefinitions {

    private final Map<String, List<Notification>> userNotifications = new HashMap<>();
    private final Map<String, Notification> notificationMap = new HashMap<>();
    private List<Notification> lastQueryResult;
    private Exception caughtException;

    @Before
    public void setUp() {
        userNotifications.clear();
        notificationMap.clear();
        lastQueryResult = null;
        caughtException = null;
    }

    @먼저("{string}에게 {int}개의 알림이 있다")
    public void 에게_개의_알림이_있다(String userId, int count) {
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String notificationId = "notification-" + String.format("%03d", i + 1);
            Notification notification = Notification.create(
                    notificationId,
                    userId,
                    null,
                    NotificationType.SCHEDULE_UPDATED,
                    "알림 " + (i + 1),
                    "알림 내용 " + (i + 1),
                    null,
                    null,
                    null
            );
            notifications.add(notification);
            notificationMap.put(notificationId, notification);
        }
        userNotifications.put(userId, notifications);
    }

    @만약("{string}이 알림 목록을 조회한다")
    @만약("{string}가 알림 목록을 조회한다")
    public void 이_알림_목록을_조회한다(String userId) {
        lastQueryResult = userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    @그러면("{int}개의 알림이 반환된다")
    public void 개의_알림이_반환된다(int count) {
        assertThat(lastQueryResult).hasSize(count);
    }

    @먼저("{string} 알림은 읽지 않은 상태이다")
    public void 알림은_읽지_않은_상태이다(String notificationId) {
        Notification notification = notificationMap.get(notificationId);
        if (notification != null) {
            assertThat(notification.isRead()).isFalse();
        }
    }

    @만약("{string}이 {string} 알림을 읽음 처리한다")
    @만약("{string}가 {string} 알림을 읽음 처리한다")
    public void 이_알림을_읽음_처리한다(String userId, String notificationId) {
        Notification notification = notificationMap.get(notificationId);
        if (notification != null) {
            notification.markAsRead();
        }
    }

    @그러면("{string} 알림의 상태가 {string}이 된다")
    @그러면("{string} 알림의 상태가 {string}가 된다")
    public void 알림의_상태가_이_된다(String notificationId, String status) {
        Notification notification = notificationMap.get(notificationId);
        if ("읽음".equals(status)) {
            assertThat(notification.isRead()).isTrue();
        }
    }

    @먼저("{string} 클럽이 {string}과 경기 일정을 등록한다")
    @먼저("{string} 클럽이 {string}와 경기 일정을 등록한다")
    public void 클럽이_과_경기_일정을_등록한다(String homeClub, String awayClub) {
        String notificationId = UUID.randomUUID().toString();
        Notification notification = Notification.create(
                notificationId,
                null,
                awayClub,
                NotificationType.SCHEDULE_LINK_REQUEST,
                "경기 일정 연계 요청",
                homeClub + "에서 경기 일정 연계를 요청했습니다.",
                null,
                "LINK_SCHEDULE",
                "/schedules/link/" + notificationId
        );
        notificationMap.put(notificationId, notification);
        userNotifications.computeIfAbsent(awayClub + "-staff", k -> new ArrayList<>()).add(notification);
    }

    @그러면("{string} 클럽의 스태프에게 일정 연계 요청 알림이 생성된다")
    public void 클럽의_스태프에게_일정_연계_요청_알림이_생성된다(String clubName) {
        List<Notification> staffNotifications = userNotifications.get(clubName + "-staff");
        assertThat(staffNotifications).isNotEmpty();
        assertThat(staffNotifications.get(0).getType()).isEqualTo(NotificationType.SCHEDULE_LINK_REQUEST);
    }

    @그리고("{string} 관련 알림이 {int}개 있다")
    public void 관련_알림이_개_있다(String clubName, int count) {
        List<Notification> clubNotifications = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String notificationId = "club-notification-" + clubName + "-" + i;
            Notification notification = Notification.create(
                    notificationId,
                    null,
                    clubName,
                    NotificationType.SCHEDULE_UPDATED,
                    clubName + " 알림 " + (i + 1),
                    "클럽 관련 알림 내용",
                    null,
                    null,
                    null
            );
            clubNotifications.add(notification);
            notificationMap.put(notificationId, notification);
        }
        userNotifications.put(clubName + "-club", clubNotifications);
    }

    @만약("{string}이 {string} 클럽의 알림을 조회한다")
    @만약("{string}가 {string} 클럽의 알림을 조회한다")
    public void 이_클럽의_알림을_조회한다(String userId, String clubName) {
        lastQueryResult = userNotifications.getOrDefault(clubName + "-club", new ArrayList<>());
    }
}
