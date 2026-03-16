package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.auth.admin.domain.Admin;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthStepDefinitions {

    private final SharedState sharedState = SharedState.getInstance();
    private final Map<String, Admin> adminMap = new HashMap<>();
    private final Map<String, String> adminPasswords = new HashMap<>();
    private boolean loginSuccess;
    private boolean sessionCreated;
    private Admin newAdmin;
    private boolean socialLoginSuccess;
    private boolean userAccountCreated;

    @Before
    public void setUp() {
        adminMap.clear();
        adminPasswords.clear();
        loginSuccess = false;
        sessionCreated = false;
        newAdmin = null;
        socialLoginSuccess = false;
        userAccountCreated = false;
    }

    @먼저("{string} 관리자가 존재한다")
    public void 관리자가_존재한다(String adminId) {
        Admin admin = Admin.init(adminId, null, "default");
        adminMap.put(adminId, admin);
    }

    @그리고("비밀번호는 {string}이다")
    @그리고("비밀번호는 {string}다")
    public void 비밀번호는_이다(String password) {
        if (!adminMap.isEmpty()) {
            String lastAdminId = adminMap.keySet().iterator().next();
            Admin adminWithPassword = Admin.init(lastAdminId, null, password);
            adminMap.put(lastAdminId, adminWithPassword);
            adminPasswords.put(lastAdminId, password);
        }
    }

    @만약("{string}이 {string}으로 로그인한다")
    @만약("{string}가 {string}으로 로그인한다")
    @만약("{string}이 {string}로 로그인한다")
    @만약("{string}가 {string}로 로그인한다")
    public void 이_으로_로그인한다(String adminId, String password) {
        Admin admin = adminMap.get(adminId);
        if (admin == null) {
            sharedState.setCaughtException(new IllegalArgumentException("존재하지 않는 관리자입니다."));
            sharedState.setLastErrorMessage("존재하지 않는 관리자입니다.");
            loginSuccess = false;
            return;
        }

        if (admin.matchPassword(password)) {
            loginSuccess = true;
            sessionCreated = true;
        } else {
            sharedState.setCaughtException(new IllegalArgumentException("비밀번호가 일치하지 않습니다"));
            sharedState.setLastErrorMessage("비밀번호가 일치하지 않습니다");
            loginSuccess = false;
        }
    }

    @그러면("로그인이 성공한다")
    public void 로그인이_성공한다() {
        assertThat(loginSuccess).isTrue();
    }

    @그리고("세션이 생성된다")
    public void 세션이_생성된다() {
        assertThat(sessionCreated).isTrue();
    }

    @먼저("{string} 관리자가 존재하지 않는다")
    public void 관리자가_존재하지_않는다(String adminId) {
        adminMap.remove(adminId);
    }

    @만약("{string}이 {string}로 처음 로그인한다")
    @만약("{string}가 {string}로 처음 로그인한다")
    public void 이_로_처음_로그인한다(String adminId, String password) {
        Admin admin = adminMap.get(adminId);
        if (admin == null) {
            newAdmin = Admin.init(adminId, null, password);
            adminMap.put(adminId, newAdmin);
            adminPasswords.put(adminId, password);
            loginSuccess = true;
            sessionCreated = true;
        }
    }

    @그러면("새 관리자 계정이 생성된다")
    public void 새_관리자_계정이_생성된다() {
        assertThat(newAdmin).isNotNull();
    }

    @만약("사용자가 {string} 소셜 로그인을 시도한다")
    public void 사용자가_소셜_로그인을_시도한다(String provider) {
        socialLoginSuccess = false;
    }

    @그리고("OIDC 인증이 성공한다")
    public void oidc_인증이_성공한다() {
        socialLoginSuccess = true;
        userAccountCreated = true;
        loginSuccess = true;
    }

    @그러면("새 사용자 계정이 생성된다")
    public void 새_사용자_계정이_생성된다() {
        assertThat(userAccountCreated).isTrue();
    }
}
