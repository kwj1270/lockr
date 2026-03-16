package com.official.lockr.domain.club.feed.infrastructure;

import com.official.lockr.domain.club.feed.domain.FeedReportRepository;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
public class JOOQFeedReportRepository implements FeedReportRepository {

    private final Configuration configuration;

    public JOOQFeedReportRepository(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Transactional
    @Override
    public void save(final String id, final String feedId, final String clubId, final String reporterUserId, final String reason) {
        DSL.using(configuration)
                .insertInto(table("feed_reports"))
                .set(field("id"), id)
                .set(field("feed_id"), feedId)
                .set(field("club_id"), clubId)
                .set(field("reporter_user_id"), reporterUserId)
                .set(field("reason"), reason)
                .onDuplicateKeyIgnore()
                .execute();
    }
}