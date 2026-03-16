package com.official.lockr.domain.club.schedule.infrastructure;

import com.official.lockr.domain.club.schedule.domain.ScheduleCommentRepository;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class JOOQScheduleCommentRepository implements ScheduleCommentRepository {

    private final Configuration configuration;

    public JOOQScheduleCommentRepository(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Transactional
    @Override
    public void save(final String id, final String scheduleId, final String clubId, final String userId, final String content) {
        DSL.using(configuration)
                .insertInto(table("schedule_comments"))
                .set(field("id"), id)
                .set(field("schedule_id"), scheduleId)
                .set(field("club_id"), clubId)
                .set(field("user_id"), userId)
                .set(field("content"), content)
                .execute();
    }
}
