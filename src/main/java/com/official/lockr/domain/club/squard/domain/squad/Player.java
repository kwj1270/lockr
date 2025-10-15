package com.official.lockr.domain.club.squard.domain.squad;

import com.official.lockr.domain.club.common.Foot;
import com.official.lockr.domain.club.common.Position;
import com.official.lockr.domain.club.squard.domain.RecruitmentInfo;
import com.official.lockr.domain.club.squard.domain.vo.BackNumber;
import com.official.lockr.domain.club.squard.domain.vo.PlayerType;

import java.time.LocalDateTime;
import java.util.List;

public class Player {

    private final String id;
    private final String squadId;
    private final String memberId;
    private final String profileImage;
    private final String name;
    private final String nationality;
    private final List<Position> positions;
    private final String birth;
    private final String height;
    private final String weight;
    private final Foot foot;
    private final BackNumber backNumber;
    private final PlayerType playerType;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public Player(final String id,
                  final String squadId,
                  final String memberId,
                  final String profileImage,
                  final String name,
                  final String nationality,
                  final List<Position> positions,
                  final String birth,
                  final String height,
                  final String weight,
                  final Foot foot,
                  final BackNumber backNumber,
                  final PlayerType playerType,
                  final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.squadId = squadId;
        this.memberId = memberId;
        this.profileImage = profileImage;
        this.name = name;
        this.nationality = nationality;
        this.positions = positions;
        this.birth = birth;
        this.height = height;
        this.weight = weight;
        this.foot = foot;
        this.backNumber = backNumber;
        this.playerType = playerType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Player init(final String id, final String squadId, final String memberId, final BackNumber backNumber) {
        return new Player(
                id, squadId, memberId, null, null, null, null, null, null, null, null, backNumber,
                PlayerType.BASIC, LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    public static Player init(final String id, final String squadId, final String memberId, final RecruitmentInfo recruitmentInfo, final BackNumber backNumber) {
        return new Player(
                id,
                squadId,
                memberId,
                recruitmentInfo.getProfileImage(),
                recruitmentInfo.getName(),
                recruitmentInfo.getNationality(),
                recruitmentInfo.getPositions(),
                recruitmentInfo.getBirth(),
                recruitmentInfo.getHeight(),
                recruitmentInfo.getWeight(),
                recruitmentInfo.getFoot(),
                backNumber,
                PlayerType.BASIC,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null);
    }

    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public String getBirth() {
        return birth;
    }

    public String getHeight() {
        return height;
    }

    public String getWeight() {
        return weight;
    }

    public Foot getFoot() {
        return foot;
    }

    public BackNumber getBackNumber() {
        return backNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public String getSquadId() {
        return squadId;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public boolean isSameMember(final String memberId) {
        return this.memberId.equals(memberId);
    }
}
