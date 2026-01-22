package com.official.lockr.domain.users.api;

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
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.generated.tables.UsersJOOQEntity.USERS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UsersApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void setUp() {
        dsl.truncate(MEMBERS).execute();
        dsl.truncate(CLUBS).execute();
        dsl.truncate(USER_ADDITIONAL_INFO).execute();
        dsl.truncate(USERS).execute();
    }

    @Test
    @DisplayName("회원 탈퇴 API 호출 성공 시 200 응답")
    void shouldReturn200WhenWithdrawSuccess() throws Exception {
        // given: 클럽에 가입되지 않은 회원
        String userId = "user-to-withdraw";
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(USERS)
                .set(USERS.ID, userId)
                .set(USERS.CREATED_AT, now)
                .set(USERS.UPDATED_AT, now)
                .execute();

        dsl.insertInto(USER_ADDITIONAL_INFO)
                .set(USER_ADDITIONAL_INFO.ID, "info-1")
                .set(USER_ADDITIONAL_INFO.USER_ID, userId)
                .set(USER_ADDITIONAL_INFO.CREATED_AT, now)
                .set(USER_ADDITIONAL_INFO.UPDATED_AT, now)
                .execute();

        // when & then
        mockMvc.perform(delete("/api/v1/users/me")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("클럽에 가입된 회원이 탈퇴 API 호출 시 400 응답")
    void shouldReturn400WhenUserHasClubMembership() throws Exception {
        // given: 클럽에 가입된 회원
        String userId = "user-with-club";
        String clubId = "club-1";
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(USERS)
                .set(USERS.ID, userId)
                .set(USERS.CREATED_AT, now)
                .set(USERS.UPDATED_AT, now)
                .execute();

        dsl.insertInto(USER_ADDITIONAL_INFO)
                .set(USER_ADDITIONAL_INFO.ID, "info-1")
                .set(USER_ADDITIONAL_INFO.USER_ID, userId)
                .set(USER_ADDITIONAL_INFO.CREATED_AT, now)
                .set(USER_ADDITIONAL_INFO.UPDATED_AT, now)
                .execute();

        dsl.insertInto(CLUBS)
                .set(CLUBS.ID, clubId)
                .set(CLUBS.FOUND_USER_ID, userId)
                .set(CLUBS.NAME, "테스트 클럽")
                .set(CLUBS.SPORT_TYPE, "SOCCER")
                .set(CLUBS.CITY, "서울")
                .set(CLUBS.DISTRICT, "강남구")
                .set(CLUBS.DESCRIPTION, "테스트")
                .set(CLUBS.CREATED_AT, now)
                .set(CLUBS.UPDATED_AT, now)
                .execute();

        dsl.insertInto(MEMBERS)
                .set(MEMBERS.ID, "member-1")
                .set(MEMBERS.USER_ID, userId)
                .set(MEMBERS.CLUB_ID, clubId)
                .set(MEMBERS.MEMBER_ROLE, "PRESIDENT")
                .set(MEMBERS.CREATED_AT, now)
                .set(MEMBERS.UPDATED_AT, now)
                .execute();

        // when & then
        mockMvc.perform(delete("/api/v1/users/me")
                        .sessionAttr("signIn", createSignInSession(userId)))
                .andExpect(status().isBadRequest());
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
