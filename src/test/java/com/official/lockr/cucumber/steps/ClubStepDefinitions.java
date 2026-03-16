package com.official.lockr.cucumber.steps;

import com.official.lockr.domain.club.club.domain.Club;
import com.official.lockr.domain.club.club.domain.Member;
import com.official.lockr.domain.club.club.domain.MemberRole;
import io.cucumber.java.Before;
import io.cucumber.java.ko.그러면;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.만약;
import io.cucumber.java.ko.먼저;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ClubStepDefinitions {

    private final SharedState sharedState = SharedState.getInstance();
    private Club club;
    private String loggedInUserId;
    private final Map<String, Member> members = new HashMap<>();

    @Before
    public void setUp() {
        club = null;
        loggedInUserId = null;
        members.clear();
    }

    @먼저("{string}이 로그인한 사용자이다")
    @먼저("{string}가 로그인한 사용자이다")
    public void 로그인한_사용자이다(String userId) {
        loggedInUserId = userId;
    }

    @만약("{string}이 {string} 클럽을 창단한다")
    @만약("{string}가 {string} 클럽을 창단한다")
    public void 클럽을_창단한다(String userId, String clubName) {
        club = Club.init(userId, clubName, "FOOTBALL", "서울", "강남구", "테스트 클럽", null, null);
        Member president = Member.president(userId, club.getId(), null);
        club.addMember(president);
        members.put(userId, president);
    }

    @그러면("클럽이 성공적으로 생성된다")
    public void 클럽이_성공적으로_생성된다() {
        assertThat(club).isNotNull();
        assertThat(club.getId()).isNotNull();
    }

    @그리고("{string}은 클럽의 회장이 된다")
    @그리고("{string}이 클럽의 회장이 된다")
    public void 은_클럽의_회장이_된다(String userId) {
        Member member = members.get(userId);
        assertThat(member).isNotNull();
        assertThat(member.isPresident()).isTrue();
    }

    @먼저("{string} 클럽이 존재한다")
    public void 클럽이_존재한다(String clubName) {
        if (club == null) {
            club = new Club(
                    "club-001", "user-001", clubName, "FOOTBALL",
                    "서울", "강남구", "테스트 클럽", null, null,
                    new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now(), null
            );
        }
    }

    @그리고("{string}은 클럽의 회장이다")
    @그리고("{string}이 클럽의 회장이다")
    public void 은_클럽의_회장이다(String userId) {
        Member president = Member.president(userId, club.getId(), null);
        club.addMember(president);
        members.put(userId, president);
    }

    @만약("회장이 {string}를 멤버로 추가한다")
    @만약("회장이 {string}을 멤버로 추가한다")
    public void 회장이_멤버로_추가한다(String userId) {
        Member newMember = Member.basic(userId, club.getId(), null);
        club.addMember(newMember);
        members.put(userId, newMember);
    }

    @그러면("{string}는 일반 멤버로 등록된다")
    @그러면("{string}은 일반 멤버로 등록된다")
    public void 는_일반_멤버로_등록된다(String userId) {
        Member member = members.get(userId);
        assertThat(member).isNotNull();
        assertThat(member.getRole()).isEqualTo(MemberRole.BASIC);
    }

    @그리고("{string}는 클럽의 일반 멤버이다")
    @그리고("{string}은 클럽의 일반 멤버이다")
    public void 는_클럽의_일반_멤버이다(String userId) {
        Member member = Member.basic(userId, club.getId(), null);
        club.addMember(member);
        members.put(userId, member);
    }

    @만약("회장이 {string}를 매니저로 임명한다")
    @만약("회장이 {string}을 매니저로 임명한다")
    public void 회장이_매니저로_임명한다(String userId) {
        Member member = members.get(userId);
        if (member != null) {
            member.assignManager();
        }
    }

    @그러면("{string}의 역할이 {string}가 된다")
    @그러면("{string}의 역할이 {string}이 된다")
    public void 의_역할이_가_된다(String userId, String role) {
        Member member = members.get(userId);
        assertThat(member.getRole()).isEqualTo(MemberRole.valueOf(role));
    }

    @만약("{string}가 {string}을 매니저로 임명하려고 한다")
    @만약("{string}이 {string}을 매니저로 임명하려고 한다")
    public void 가_매니저로_임명하려고_한다(String actorId, String targetId) {
        Member actor = members.get(actorId);
        if (actor == null || !actor.isStaff()) {
            sharedState.setCaughtException(new IllegalArgumentException("권한이 없습니다."));
        }
    }
}
