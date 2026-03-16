package com.official.lockr.domain.home.card.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.jooq.generated.tables.ClubsJOOQEntity.CLUBS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.generated.tables.UserPinnedClubsJOOQEntity.USER_PINNED_CLUBS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class HomeCardApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void setUp() {
        dsl.truncate(SCHEDULES).execute();
        dsl.truncate(USER_PINNED_CLUBS).execute();
        dsl.truncate(MEMBERS).execute();
        dsl.truncate(CLUBS).execute();
    }

    @Test
    @DisplayName("핀된 클럽이 없으면 빈 배열 반환")
    void shouldReturnEmptyCardsWhenNoPinnedClubs() throws Exception {
        // given: 로그인된 사용자가 있고, 핀된 클럽이 없는 상태

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession("user-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isEmpty());
    }

    @Test
    @DisplayName("핀된 클럽이 1개면 1개 카드 반환")
    void shouldReturnOneCardWhenOnePinnedClub() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        // 클럽 생성
        dsl.insertInto(CLUBS)
                .set(CLUBS.ID, clubId)
                .set(CLUBS.FOUND_USER_ID, userId)
                .set(CLUBS.NAME, "테스트 클럽")
                .set(CLUBS.SPORT_TYPE, "SOCCER")
                .set(CLUBS.CITY, "서울")
                .set(CLUBS.DISTRICT, "강남구")
                .set(CLUBS.DESCRIPTION, "테스트 클럽입니다")
                .set(CLUBS.CREATED_AT, now)
                .set(CLUBS.UPDATED_AT, now)
                .execute();

        // 멤버 가입
        dsl.insertInto(MEMBERS)
                .set(MEMBERS.ID, "member-1")
                .set(MEMBERS.USER_ID, userId)
                .set(MEMBERS.CLUB_ID, clubId)
                .set(MEMBERS.MEMBER_ROLE, "PRESIDENT")
                .set(MEMBERS.CREATED_AT, now)
                .set(MEMBERS.UPDATED_AT, now)
                .execute();

        // 클럽 핀 설정
        dsl.insertInto(USER_PINNED_CLUBS)
                .set(USER_PINNED_CLUBS.ID, "pin-1")
                .set(USER_PINNED_CLUBS.USER_ID, userId)
                .set(USER_PINNED_CLUBS.CLUB_ID, clubId)
                .set(USER_PINNED_CLUBS.PIN_ORDER, 1)
                .set(USER_PINNED_CLUBS.CREATED_AT, now)
                .execute();

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId))
                .andExpect(jsonPath("$.cards[0].name").value("테스트 클럽"));
    }

    @Test
    @DisplayName("핀된 클럽이 2개면 2개 카드 반환")
    void shouldReturnTwoCardsWhenTwoPinnedClubs() throws Exception {
        // given
        String userId = "user-1";
        String clubId1 = "club-1";
        String clubId2 = "club-2";
        LocalDateTime now = LocalDateTime.now();

        // 클럽 2개 생성
        createClub(clubId1, userId, "첫번째 클럽", "SOCCER", now);
        createClub(clubId2, userId, "두번째 클럽", "BASEBALL", now);

        // 멤버 가입
        createMember("member-1", userId, clubId1, "PRESIDENT", now);
        createMember("member-2", userId, clubId2, "MEMBER", now);

        // 클럽 핀 설정 (순서: club-2가 먼저, club-1이 나중)
        createPin("pin-1", userId, clubId2, 1, now);
        createPin("pin-2", userId, clubId1, 2, now);

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards.length()").value(2))
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId2))
                .andExpect(jsonPath("$.cards[0].name").value("두번째 클럽"))
                .andExpect(jsonPath("$.cards[1].clubId").value(clubId1))
                .andExpect(jsonPath("$.cards[1].name").value("첫번째 클럽"));
    }

    @Test
    @DisplayName("카드에 클럽 기본 정보(id, name, emblemUrl, sport, location) 포함")
    void shouldIncludeClubBasicInfoInCard() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        String emblemUrl = "https://example.com/emblem.png";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "FC 서울", "SOCCER", "서울", "강남구", emblemUrl, now);
        createMember("member-1", userId, clubId, "PRESIDENT", now);
        createPin("pin-1", userId, clubId, 1, now);

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId))
                .andExpect(jsonPath("$.cards[0].name").value("FC 서울"))
                .andExpect(jsonPath("$.cards[0].emblemUrl").value(emblemUrl))
                .andExpect(jsonPath("$.cards[0].sport").value("SOCCER"))
                .andExpect(jsonPath("$.cards[0].location").value("서울 강남구"));
    }

    @Test
    @DisplayName("카드에 사용자 역할(userRole) 포함")
    void shouldIncludeUserRoleInCard() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        createMember("member-1", userId, clubId, "MANAGER", now);
        createPin("pin-1", userId, clubId, 1, now);

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].userRole").value("MANAGER"));
    }

    @Test
    @DisplayName("카드에 멤버 수(memberCount) 포함")
    void shouldIncludeMemberCountInCard() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        // 3명의 멤버 추가
        createMember("member-1", userId, clubId, "PRESIDENT", now);
        createMember("member-2", "user-2", clubId, "MEMBER", now);
        createMember("member-3", "user-3", clubId, "MEMBER", now);
        createPin("pin-1", userId, clubId, 1, now);

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].memberCount").value(3));
    }

    @Test
    @DisplayName("카드에 배경색(backgroundColor) 포함")
    void shouldIncludeBackgroundColorInCard() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        String backgroundColor = "#FF5733";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        createMember("member-1", userId, clubId, "PRESIDENT", now);
        createPin("pin-1", userId, clubId, 1, backgroundColor, now);

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].backgroundColor").value(backgroundColor));
    }

    @Test
    @DisplayName("카드에 다음 일정(nextScheduleDate) 포함 - 일정 있는 경우")
    void shouldIncludeNextScheduleDateWhenScheduleExists() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduleTime = now.plusDays(7);

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        createMember("member-1", userId, clubId, "PRESIDENT", now);
        createPin("pin-1", userId, clubId, 1, now);
        createSchedule("schedule-1", clubId, "훈련", scheduleTime, now);

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].nextScheduleDate").exists());
    }

    @Test
    @DisplayName("다음 일정이 없으면 nextScheduleDate는 null")
    void shouldReturnNullNextScheduleDateWhenNoSchedule() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        createMember("member-1", userId, clubId, "PRESIDENT", now);
        createPin("pin-1", userId, clubId, 1, now);
        // 일정 없음

        // when & then
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].nextScheduleDate").doesNotExist());
    }

    // ==================== 핀 설정 API 테스트 ====================

    @Test
    @DisplayName("클럽 1개 핀 설정 성공")
    void shouldPinOneClubSuccessfully() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        createMember("member-1", userId, clubId, "MEMBER", now);

        String requestBody = """
            {
                "clubIds": ["%s"]
            }
            """.formatted(clubId);

        // when & then
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // 핀이 설정되었는지 확인
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId));
    }

    @Test
    @DisplayName("클럽 2개 핀 설정 성공")
    void shouldPinTwoClubsSuccessfully() throws Exception {
        // given
        String userId = "user-1";
        String clubId1 = "club-1";
        String clubId2 = "club-2";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId1, userId, "첫번째 클럽", "SOCCER", now);
        createClub(clubId2, userId, "두번째 클럽", "BASEBALL", now);
        createMember("member-1", userId, clubId1, "MEMBER", now);
        createMember("member-2", userId, clubId2, "MEMBER", now);

        String requestBody = """
            {
                "clubIds": ["%s", "%s"]
            }
            """.formatted(clubId1, clubId2);

        // when & then
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // 핀이 설정되었는지 확인
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(2))
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId1))
                .andExpect(jsonPath("$.cards[1].clubId").value(clubId2));
    }

    @Test
    @DisplayName("3개 이상 핀 설정 시 실패 (최대 2개 제한)")
    void shouldFailWhenPinMoreThanTwoClubs() throws Exception {
        // given
        String userId = "user-1";
        String clubId1 = "club-1";
        String clubId2 = "club-2";
        String clubId3 = "club-3";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId1, userId, "첫번째 클럽", "SOCCER", now);
        createClub(clubId2, userId, "두번째 클럽", "BASEBALL", now);
        createClub(clubId3, userId, "세번째 클럽", "BASKETBALL", now);
        createMember("member-1", userId, clubId1, "MEMBER", now);
        createMember("member-2", userId, clubId2, "MEMBER", now);
        createMember("member-3", userId, clubId3, "MEMBER", now);

        String requestBody = """
            {
                "clubIds": ["%s", "%s", "%s"]
            }
            """.formatted(clubId1, clubId2, clubId3);

        // when & then
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("핀 순서가 요청 순서대로 유지됨")
    void shouldMaintainPinOrder() throws Exception {
        // given
        String userId = "user-1";
        String clubId1 = "club-1";
        String clubId2 = "club-2";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId1, userId, "첫번째 클럽", "SOCCER", now);
        createClub(clubId2, userId, "두번째 클럽", "BASEBALL", now);
        createMember("member-1", userId, clubId1, "MEMBER", now);
        createMember("member-2", userId, clubId2, "MEMBER", now);

        // club2를 먼저, club1을 나중에 설정
        String requestBody = """
            {
                "clubIds": ["%s", "%s"]
            }
            """.formatted(clubId2, clubId1);

        // when
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // then - 순서가 요청대로 club2, club1 순서로 반환
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId2))
                .andExpect(jsonPath("$.cards[1].clubId").value(clubId1));
    }

    @Test
    @DisplayName("기존 핀을 새로운 핀으로 교체")
    void shouldReplaceExistingPinsWithNewPins() throws Exception {
        // given
        String userId = "user-1";
        String clubId1 = "club-1";
        String clubId2 = "club-2";
        String clubId3 = "club-3";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId1, userId, "첫번째 클럽", "SOCCER", now);
        createClub(clubId2, userId, "두번째 클럽", "BASEBALL", now);
        createClub(clubId3, userId, "세번째 클럽", "BASKETBALL", now);
        createMember("member-1", userId, clubId1, "MEMBER", now);
        createMember("member-2", userId, clubId2, "MEMBER", now);
        createMember("member-3", userId, clubId3, "MEMBER", now);

        // 먼저 club1, club2를 핀 설정
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {"clubIds": ["%s", "%s"]}
                            """.formatted(clubId1, clubId2))
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // when - club2, club3으로 교체
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {"clubIds": ["%s", "%s"]}
                            """.formatted(clubId2, clubId3))
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // then - 새로운 핀만 남아야 함
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(2))
                .andExpect(jsonPath("$.cards[0].clubId").value(clubId2))
                .andExpect(jsonPath("$.cards[1].clubId").value(clubId3));
    }

    @Test
    @DisplayName("빈 배열로 모든 핀 해제")
    void shouldUnpinAllWhenEmptyArray() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        createClub(clubId, userId, "테스트 클럽", "SOCCER", now);
        createMember("member-1", userId, clubId, "MEMBER", now);

        // 먼저 핀 설정
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {"clubIds": ["%s"]}
                            """.formatted(clubId))
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // when - 빈 배열로 핀 해제
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {"clubIds": []}
                            """)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());

        // then - 핀이 모두 해제됨
        mockMvc.perform(get("/api/v1/home/cards")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isEmpty());
    }

    @Test
    @DisplayName("가입하지 않은 클럽은 핀 설정 불가")
    void shouldFailWhenPinClubNotMemberOf() throws Exception {
        // given
        String userId = "user-1";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        // 클럽은 존재하지만, 사용자는 해당 클럽의 멤버가 아님
        createClub(clubId, "other-user", "테스트 클럽", "SOCCER", now);

        String requestBody = """
            {
                "clubIds": ["%s"]
            }
            """.formatted(clubId);

        // when & then
        mockMvc.perform(put("/api/v1/home/cards/pin")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isBadRequest());
    }

    private void createClub(String clubId, String foundUserId, String name, String sportType, LocalDateTime now) {
        createClub(clubId, foundUserId, name, sportType, "서울", "강남구", null, now);
    }

    private void createClub(String clubId, String foundUserId, String name, String sportType,
                            String city, String district, String emblemUrl, LocalDateTime now) {
        dsl.insertInto(CLUBS)
                .set(CLUBS.ID, clubId)
                .set(CLUBS.FOUND_USER_ID, foundUserId)
                .set(CLUBS.NAME, name)
                .set(CLUBS.SPORT_TYPE, sportType)
                .set(CLUBS.CITY, city)
                .set(CLUBS.DISTRICT, district)
                .set(CLUBS.PROFILE_IMAGE_URL, emblemUrl)
                .set(CLUBS.DESCRIPTION, name + " 설명")
                .set(CLUBS.CREATED_AT, now)
                .set(CLUBS.UPDATED_AT, now)
                .execute();
    }

    private void createMember(String memberId, String userId, String clubId, String role, LocalDateTime now) {
        dsl.insertInto(MEMBERS)
                .set(MEMBERS.ID, memberId)
                .set(MEMBERS.USER_ID, userId)
                .set(MEMBERS.CLUB_ID, clubId)
                .set(MEMBERS.MEMBER_ROLE, role)
                .set(MEMBERS.CREATED_AT, now)
                .set(MEMBERS.UPDATED_AT, now)
                .execute();
    }

    private void createPin(String pinId, String userId, String clubId, int order, LocalDateTime now) {
        createPin(pinId, userId, clubId, order, null, now);
    }

    private void createPin(String pinId, String userId, String clubId, int order, String backgroundColor, LocalDateTime now) {
        dsl.insertInto(USER_PINNED_CLUBS)
                .set(USER_PINNED_CLUBS.ID, pinId)
                .set(USER_PINNED_CLUBS.USER_ID, userId)
                .set(USER_PINNED_CLUBS.CLUB_ID, clubId)
                .set(USER_PINNED_CLUBS.PIN_ORDER, order)
                .set(USER_PINNED_CLUBS.BACKGROUND_COLOR, backgroundColor)
                .set(USER_PINNED_CLUBS.CREATED_AT, now)
                .execute();
    }

    private void createSchedule(String scheduleId, String clubId, String title, LocalDateTime scheduleTime, LocalDateTime now) {
        dsl.insertInto(SCHEDULES)
                .set(SCHEDULES.ID, scheduleId)
                .set(SCHEDULES.CLUB_ID, clubId)
                .set(SCHEDULES.TITLE, title)
                .set(SCHEDULES.SCHEDULE_TIME, scheduleTime)
                .set(SCHEDULES.TYPE, "TRAINING")
                .set(SCHEDULES.STATUS, "SCHEDULED")
                .set(SCHEDULES.CREATED_AT, now)
                .set(SCHEDULES.UPDATED_AT, now)
                .execute();
    }

    private SignInSession createSignInSession(String userId) {
        return new SignInSession(
                userId,
                "device-1", "TestDevice", "TestOS",
                "127.0.0.1", "TestAgent",
                LocalDateTime.now()
        );
    }
}