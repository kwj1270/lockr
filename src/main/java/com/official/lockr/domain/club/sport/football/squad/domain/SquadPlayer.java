package com.official.lockr.domain.club.sport.football.squad.domain;

import com.official.lockr.global.vo.BirthDate;
import com.official.lockr.global.vo.Position;
import com.official.lockr.domain.club.recruitment.applications.domain.Application;
import com.official.lockr.domain.club.recruitment.applications.domain.vo.sport.FootballSportSpecificData;
import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.Foot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SquadPlayer {

    private final String id;
    private final String squadId;
    private final String userId;
    private BirthDate birthDate;
    private String height;
    private String weight;
    private Foot foot;
    private List<Position> positions;
    private BackNumber backNumber;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    public SquadPlayer(final String id,
                       final String squadId,
                       final String userId,
                       final BirthDate birthDate,
                       final String height,
                       final String weight,
                       final Foot foot,
                       final List<Position> positions,
                       final BackNumber backNumber,
                       final LocalDateTime createdAt, final LocalDateTime updatedAt, final LocalDateTime deletedAt
    ) {
        this.id = id;
        this.squadId = squadId;
        this.userId = userId;
        this.positions = positions;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.foot = foot;
        this.backNumber = backNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static SquadPlayer init(final String id, final String squadId, final String userId, final BackNumber backNumber) {
        return new SquadPlayer(
                id, squadId, userId, null, null, null, null, null, backNumber,
                LocalDateTime.now(), LocalDateTime.now(), null
        );
    }

    public static SquadPlayer init(final String id, final String squadId, final String userId, final Application application, final FootballSportSpecificData footballSportSpecificData, final BackNumber backNumber) {
        return new SquadPlayer(
                id,
                squadId,
                userId,
                application.getBirthDate(),
                footballSportSpecificData.height(),
                footballSportSpecificData.weight(),
                Foot.valueOf(footballSportSpecificData.foot()),
                List.of(Position.valueOf(footballSportSpecificData.position())),
                backNumber,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null);
    }


    public void update(final String birthDate, final String height, final String weight, final Foot foot, final List<Position> positions, final BackNumber backNumber) {
        this.birthDate = birthDate != null ? new BirthDate(birthDate) : this.birthDate;
        this.height = height;
        this.weight = weight;
        this.foot = foot;
        this.positions = positions;
        this.backNumber = backNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public BirthDate getBirthDate() {
        return birthDate;
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

    public boolean isSame(final String userId) {
        return this.userId.equals(userId);
    }
}
