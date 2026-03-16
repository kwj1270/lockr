package com.official.lockr.domain.auth.signin.api;

import com.official.lockr.global.http.HttpHeaderContext;
import com.official.lockr.global.http.HttpHeaders;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.tables.AdminJOOQEntity.ADMIN;
import static org.jooq.generated.tables.SignInJOOQEntity.SIGN_IN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class SignInAdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        dsl.truncate(SIGN_IN).execute();
        dsl.truncate(ADMIN).execute();
        httpHeaders.set(new HttpHeaderContext(
                null, null, null, null, null, null, null,
                "test-device-id", "TestDevice", "TestOS", "127.0.0.1", null
        ));
    }

    @AfterEach
    void tearDown() {
        httpHeaders.remove();
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign_in/admin 성공 시 200과 Admin 정보를 반환한다")
    void shouldReturn200AndAdminInfoWhenLoginSuccess() throws Exception {
        // given: 기존 관리자가 존재
        String adminId = "testAdmin";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        String userId = "01HTEST0000000000000000001";
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(ADMIN)
                .set(ADMIN.ID, adminId)
                .set(ADMIN.PASSWORD, encodedPassword)
                .set(ADMIN.USER_ID, userId)
                .set(ADMIN.ROLE, "BASIC")
                .set(ADMIN.CREATED_AT, now)
                .set(ADMIN.UPDATED_AT, now)
                .execute();

        String requestBody = """
                {
                    "id": "%s",
                    "password": "%s"
                }
                """.formatted(adminId, rawPassword);

        // when & then
        mockMvc.perform(post("/api/v1/auth/sign_in/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "test-device-id")
                        .header("X-Device-Name", "TestDevice")
                        .header("X-Device-OS", "TestOS")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adminId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.role").value("BASIC"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign_in/admin 성공 시 세션 쿠키가 설정되고 SignIn 레코드가 생성된다")
    void shouldSetSessionCookieAndCreateSignInRecordWhenLoginSuccess() throws Exception {
        // given: 기존 관리자가 존재
        String adminId = "testAdmin";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        String userId = "01HTEST0000000000000000001";
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(ADMIN)
                .set(ADMIN.ID, adminId)
                .set(ADMIN.PASSWORD, encodedPassword)
                .set(ADMIN.USER_ID, userId)
                .set(ADMIN.ROLE, "BASIC")
                .set(ADMIN.CREATED_AT, now)
                .set(ADMIN.UPDATED_AT, now)
                .execute();

        String requestBody = """
                {
                    "id": "%s",
                    "password": "%s"
                }
                """.formatted(adminId, rawPassword);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/sign_in/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "test-device-id")
                        .header("X-Device-Name", "TestDevice")
                        .header("X-Device-OS", "TestOS")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // then: SignIn 레코드가 생성되었는지 확인
        var signInRecord = dsl.selectFrom(SIGN_IN)
                .where(SIGN_IN.USER_ID.eq(userId))
                .fetchOne();

        assertThat(signInRecord).isNotNull();
        assertThat(signInRecord.getUserId()).isEqualTo(userId);
        assertThat(signInRecord.getDeviceId()).isEqualTo("test-device-id");
        assertThat(signInRecord.getDeviceName()).isEqualTo("TestDevice");
        assertThat(signInRecord.getDeviceOs()).isEqualTo("TestOS");
    }

    @Test
    @DisplayName("POST /api/v1/auth/sign_in/admin 비밀번호 불일치 시 401을 반환한다")
    void shouldReturn401WhenPasswordDoesNotMatch() throws Exception {
        // given: 기존 관리자가 존재
        String adminId = "testAdmin";
        String correctPassword = "password123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(correctPassword);
        String userId = "01HTEST0000000000000000001";
        LocalDateTime now = LocalDateTime.now();

        dsl.insertInto(ADMIN)
                .set(ADMIN.ID, adminId)
                .set(ADMIN.PASSWORD, encodedPassword)
                .set(ADMIN.USER_ID, userId)
                .set(ADMIN.ROLE, "BASIC")
                .set(ADMIN.CREATED_AT, now)
                .set(ADMIN.UPDATED_AT, now)
                .execute();

        String requestBody = """
                {
                    "id": "%s",
                    "password": "%s"
                }
                """.formatted(adminId, wrongPassword);

        // when & then
        mockMvc.perform(post("/api/v1/auth/sign_in/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Device-Id", "test-device-id")
                        .header("X-Device-Name", "TestDevice")
                        .header("X-Device-OS", "TestOS")
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}
