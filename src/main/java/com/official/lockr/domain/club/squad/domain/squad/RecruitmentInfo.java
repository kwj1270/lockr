package com.official.lockr.domain.club.squad.domain.squad;

import com.official.lockr.domain.club.common.Foot;
import com.official.lockr.domain.club.common.Position;

import java.util.List;

public class RecruitmentInfo {

    private final String teamId;
    private final String userId;
    private final String profileImage;
    private final String birth;
    private final String weight;
    private final String height;
    private final String name;
    private final String nationality;
    private final List<Position> positions;
    private final Foot foot;
    private final String advantages;
    private final String disadvantages;

    public RecruitmentInfo(final String teamId, final String userId, final String profileImage, final String birth, final String weight, final String height, final String name, final String nationality, final List<Position> positions, final Foot foot, final String advantages, final String disadvantages) {
        this.teamId = teamId;
        this.userId = userId;
        this.profileImage = profileImage;
        this.birth = birth;
        this.weight = weight;
        this.height = height;
        this.name = name;
        this.nationality = nationality;
        this.positions = positions;
        this.foot = foot;
        this.advantages = advantages;
        this.disadvantages = disadvantages;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getUserId() {
        return userId;
    }

    public String getBirth() {
        return birth;
    }

    public String getWeight() {
        return weight;
    }

    public String getHeight() {
        return height;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public Foot getFoot() {
        return foot;
    }

    public String getAdvantages() {
        return advantages;
    }

    public String getDisadvantages() {
        return disadvantages;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
