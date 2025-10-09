package com.official.lockr.domain.team.team.infrastructure;

import com.official.lockr.domain.team.team.domain.Member;
import com.official.lockr.domain.team.team.domain.MemberRole;
import com.official.lockr.domain.team.team.domain.Team;
import com.official.lockr.domain.team.team.domain.TeamRepository;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.InsertSetStep;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.daos.TeamsDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.jooq.generated.tables.pojos.TeamsEntity;
import org.jooq.generated.tables.records.MembersRecord;
import org.jooq.generated.tables.records.TeamsRecord;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.TeamsJOOQEntity.TEAMS;


@Repository
public class JOOQTeamRepository implements TeamRepository {

    private final TeamsDao teamDao;
    private final MembersDao memberDao;

    public JOOQTeamRepository(final Configuration configuration) {
        this.teamDao = new TeamsDao(configuration);
        this.memberDao = new MembersDao(configuration);
    }

    @Nullable
    @Override
    public Team findByName(final String name) {
        final TeamsRecord teamsRecord = teamDao.ctx().selectFrom(TEAMS)
                .where(TEAMS.NAME.eq(name))
                .fetchOne();
        if (Objects.isNull(teamsRecord)) {
            return null;
        }
        return domain(teamsRecord, findAllMember(teamsRecord.getId()));
    }

    @Transactional
    @Override
    public Team save(final Team team) {
        insertInto(team);
        insertInto(team.getMembers());
        return team;
    }

    private int insertInto(final Team team) {
        return teamDao.ctx()
                .insertInto(TEAMS)
                .set(TEAMS.ID, team.getId())
                .set(TEAMS.NAME, team.getName())
                .set(TEAMS.DESCRIPTION, team.getDescription())
                .set(TEAMS.CREATED_AT, team.getCreatedAt())
                .set(TEAMS.UPDATED_AT, team.getUpdatedAt())
                .set(TEAMS.DELETED_AT, team.getDeletedAt())
                .onConflict(TEAMS.ID).doNothing()
                .execute();
    }

    private void insertInto(final List<Member> members) {
        final InsertSetStep<MembersRecord> query = memberDao.ctx()
                .insertInto(MEMBERS);
        for (final Member member : members) {
            query.values(
                    member.getId(),
                    member.getUserId(),
                    member.getRole().name(),
                    member.getTeamId(),
                    member.getCreatedAt(),
                    member.getUpdatedAt(),
                    member.getDeletedAt()
            ).onConflict(MEMBERS.ID).doNothing().execute();
        }
    }

    @Nullable
    @Override
    public Team findById(final String id) {
        final TeamsEntity teamsEntity = teamDao.findById(id);
        if (Objects.isNull(teamsEntity)) {
            return null;
        }
        return domain(teamsEntity, findAllMember(id));
    }

    private List<Member> findAllMember(final String teamId) {
        return memberDao.ctx()
                .selectFrom(MEMBERS)
                .where(MEMBERS.TEAM_ID.eq(teamId))
                .fetchInto(MembersEntity.class)
                .stream().map(JOOQTeamRepository::domain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static Team domain(final TeamsEntity teamsEntity, final List<Member> members) {
        return new Team(
                teamsEntity.getId(),
                teamsEntity.getName(),
                teamsEntity.getDescription(),
                members,
                teamsEntity.getCreatedAt(),
                teamsEntity.getUpdatedAt(),
                teamsEntity.getDeletedAt()
        );
    }

    private static Member domain(final MembersEntity entity) {
        return new Member(
                entity.getId(),
                entity.getUserId(),
                MemberRole.valueOf(entity.getMemberRole()),
                entity.getTeamId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private Team domain(final TeamsRecord teamsRecord, final List<Member> members) {
        return new Team(
                teamsRecord.getId(),
                teamsRecord.getName(),
                teamsRecord.getDescription(),
                members,
                teamsRecord.getCreatedAt(),
                teamsRecord.getUpdatedAt(),
                teamsRecord.getDeletedAt()
        );
    }
}
