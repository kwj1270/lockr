package com.official.lockr.domain.club.team.infrastructure;

import com.official.lockr.domain.club.team.domain.Member;
import com.official.lockr.domain.club.team.domain.MemberRole;
import com.official.lockr.domain.club.team.domain.Team;
import com.official.lockr.domain.club.team.domain.TeamRepository;
import com.official.lockr.global.ddd.DomainEventPublisher;
import jakarta.annotation.Nullable;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.MembersDao;
import org.jooq.generated.tables.daos.TeamsDao;
import org.jooq.generated.tables.pojos.MembersEntity;
import org.jooq.generated.tables.pojos.TeamsEntity;
import org.jooq.generated.tables.records.TeamsRecord;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.TeamsJOOQEntity.TEAMS;
import static org.jooq.impl.DSL.excluded;

@Repository
public class JOOQTeamRepository implements TeamRepository {

    private final TeamsDao teamDao;
    private final MembersDao memberDao;
    private final DomainEventPublisher domainEventPublisher;

    public JOOQTeamRepository(final Configuration configuration,
                              final DomainEventPublisher domainEventPublisher
    ) {
        this.teamDao = new TeamsDao(configuration);
        this.memberDao = new MembersDao(configuration);
        this.domainEventPublisher = domainEventPublisher;
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
        upsertTeam(team);
        syncMembers(team);
        team.publish(domainEventPublisher);
        return team;
    }

    // ✅ INSERT or UPDATE Team
    private void upsertTeam(final Team team) {
        teamDao.ctx()
                .insertInto(TEAMS)
                .set(TEAMS.ID, team.getId())
                .set(TEAMS.NAME, team.getName())
                .set(TEAMS.DESCRIPTION, team.getDescription())
                .set(TEAMS.CREATED_AT, team.getCreatedAt())
                .set(TEAMS.UPDATED_AT, team.getUpdatedAt())
                .set(TEAMS.DELETED_AT, team.getDeletedAt())
                .onDuplicateKeyUpdate()
                .set(TEAMS.NAME, team.getName())
                .set(TEAMS.DESCRIPTION, team.getDescription())
                .set(TEAMS.UPDATED_AT, team.getUpdatedAt())
                .set(TEAMS.DELETED_AT, team.getDeletedAt())
                .execute();
    }

    // ✅ Member 동기화 (추가/수정/삭제)
    private void syncMembers(final Team team) {
        // 1. 기존 멤버 조회
        final List<String> existingMemberIds = memberDao.ctx()
                .select(MEMBERS.ID)
                .from(MEMBERS)
                .where(MEMBERS.TEAM_ID.eq(team.getId()))
                .fetchInto(String.class);

        final List<String> currentMemberIds = team.getMembers().stream()
                .map(Member::getId)
                .toList();

        // 2. 삭제할 멤버 (기존에는 있었지만 현재 없는)
        final List<String> membersToDelete = existingMemberIds.stream()
                .filter(id -> !currentMemberIds.contains(id))
                .toList();

        if (!membersToDelete.isEmpty()) {
            memberDao.ctx()
                    .deleteFrom(MEMBERS)
                    .where(MEMBERS.ID.in(membersToDelete))
                    .execute();
        }

        // 3. 추가/수정할 멤버 (UPSERT)
        if (!team.getMembers().isEmpty()) {
            upsertMembers(team.getMembers());
        }
    }

    private void upsertMembers(final List<Member> members) {
        if (members.isEmpty()) {
            return;
        }

        var query = memberDao.ctx().insertInto(MEMBERS,
                MEMBERS.ID,
                MEMBERS.USER_ID,
                MEMBERS.MEMBER_ROLE,
                MEMBERS.TEAM_ID,
                MEMBERS.CREATED_AT,
                MEMBERS.UPDATED_AT,
                MEMBERS.DELETED_AT
        );

        for (final Member member : members) {
            query.values(
                    member.getId(),
                    member.getUserId(),
                    member.getRole().name(),
                    member.getTeamId(),
                    member.getCreatedAt(),
                    member.getUpdatedAt(),
                    member.getDeletedAt()
            );
        }

        query.onDuplicateKeyUpdate()
                .set(MEMBERS.USER_ID, excluded(MEMBERS.USER_ID))
                .set(MEMBERS.MEMBER_ROLE, excluded(MEMBERS.MEMBER_ROLE))
                .set(MEMBERS.UPDATED_AT, excluded(MEMBERS.UPDATED_AT))
                .set(MEMBERS.DELETED_AT, excluded(MEMBERS.DELETED_AT))
                .execute();
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
                .stream()
                .map(JOOQTeamRepository::domain)
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
