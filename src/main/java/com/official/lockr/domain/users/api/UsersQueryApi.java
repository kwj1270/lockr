package com.official.lockr.domain.users.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderRequest;
import com.official.lockr.domain.users.api.dto.FindUsersByProviderResponse;
import com.official.lockr.domain.users.api.dto.UserActivityStatsResponse;
import com.official.lockr.domain.users.api.dto.UserAdditionalInfoResponse;
import com.official.lockr.global.vo.Gender;
import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.generated.tables.daos.PlayerMatchStatsDao;
import org.jooq.generated.tables.daos.UserAdditionalInfoDao;
import org.jooq.generated.tables.daos.UsersDao;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import static org.jooq.generated.tables.AttendancesJOOQEntity.ATTENDANCES;
import static org.jooq.generated.tables.PlayerMatchStatsJOOQEntity.PLAYER_MATCH_STATS;
import static org.jooq.generated.tables.SchedulesJOOQEntity.SCHEDULES;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;
import static org.jooq.generated.tables.UserAdditionalInfoJOOQEntity.USER_ADDITIONAL_INFO;
import static org.jooq.impl.DSL.*;

@RequestMapping(value = "/api/v1/users")
@RestController
public class UsersQueryApi {

    private final UsersDao usersDao;
    private final UserAdditionalInfoDao userAdditionalInfoDao;
    private final PlayerMatchStatsDao playerMatchStatsDao;

    public UsersQueryApi(final Configuration configuration) {
        this.usersDao = new UsersDao(configuration);
        this.userAdditionalInfoDao = new UserAdditionalInfoDao(configuration);
        this.playerMatchStatsDao = new PlayerMatchStatsDao(configuration);
    }

    @Transactional(readOnly = true)
    @GetMapping("/additional-info")
    public ResponseEntity<UserAdditionalInfoResponse> getMyAdditionalInfo(
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final var record = userAdditionalInfoDao.ctx()
                .selectFrom(USER_ADDITIONAL_INFO)
                .where(USER_ADDITIONAL_INFO.USER_ID.eq(signInSession.userId()))
                .and(USER_ADDITIONAL_INFO.DELETED_AT.isNull())
                .fetchOne();

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        final UserAdditionalInfoResponse response = UserAdditionalInfoResponse.of(
                record.getId(),
                record.getUserId(),
                record.getName(),
                record.getBirthDate(),
                record.getPhone(),
                Gender.fromDbValue(record.getGender())
        );

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    @GetMapping("/me/activity-stats")
    public ResponseEntity<UserActivityStatsResponse> getMyActivityStats(
            @RequestAttribute("signInSession") final SignInSession signInSession
    ) {
        final String userId = signInSession.userId();

        final Record statsRecord = playerMatchStatsDao.ctx()
                .select(
                        count().as("totalMatches"),
                        coalesce(sum(PLAYER_MATCH_STATS.GOALS), 0).as("totalGoals"),
                        coalesce(sum(PLAYER_MATCH_STATS.ASSISTS), 0).as("totalAssists"),
                        count(when(PLAYER_MATCH_STATS.IS_MOM.isTrue(), 1)).as("momCount")
                )
                .from(PLAYER_MATCH_STATS)
                .where(PLAYER_MATCH_STATS.USER_ID.eq(userId))
                .fetchOne();

        final int totalMatches = statsRecord.get("totalMatches", int.class);
        final int totalGoals = statsRecord.get("totalGoals", int.class);
        final int totalAssists = statsRecord.get("totalAssists", int.class);
        final int momCount = statsRecord.get("momCount", int.class);

        final int attendedCount = playerMatchStatsDao.ctx()
                .selectCount()
                .from(ATTENDANCES)
                .where(ATTENDANCES.USER_ID.eq(userId))
                .and(ATTENDANCES.STATUS.eq("ATTENDING"))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .fetchOne(0, int.class);

        final int totalSchedules = playerMatchStatsDao.ctx()
                .selectCount()
                .from(ATTENDANCES)
                .where(ATTENDANCES.USER_ID.eq(userId))
                .and(ATTENDANCES.DELETED_AT.isNull())
                .fetchOne(0, int.class);

        final double attendanceRate = totalSchedules > 0
                ? Math.round((double) attendedCount / totalSchedules * 100.0 * 10.0) / 10.0
                : 0.0;

        return ResponseEntity.ok(new UserActivityStatsResponse(
                totalMatches, totalGoals, totalAssists, attendanceRate, momCount
        ));
    }
}
