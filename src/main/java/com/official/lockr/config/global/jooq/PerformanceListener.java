package com.official.lockr.config.global.jooq;

import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.Query;
import org.jooq.tools.StopWatch;
import org.slf4j.Logger;

import java.time.Duration;

import static org.slf4j.LoggerFactory.getLogger;

public class PerformanceListener implements ExecuteListener {

    private static final Duration SLOW_QUERY_LIMIT = Duration.ofSeconds(3);
    private static final Logger log = getLogger(PerformanceListener.class);

    private StopWatch watch;

    @Override
    public void executeStart(final ExecuteContext ctx) {
        watch = new StopWatch();
    }

    @Override
    public void executeEnd(final ExecuteContext ctx) {
        final long queryTimeNano = watch.split();

        if (queryTimeNano > SLOW_QUERY_LIMIT.toNanos()) {
            final Query query = ctx.query();
            final Duration executeTime = Duration.ofNanos(queryTimeNano);
            log.warn("""
                    ### Slow SQL 탐지 >>
                    경고: jOOQ로 실행된 쿼리 중 {}초 이상 실행된 쿼리가 있습니다.
                    실행시간: {}초
                    실행쿼리: {}
                    """, SLOW_QUERY_LIMIT.toSeconds(), millisToSeconds(executeTime), query);
        }
    }

    private String millisToSeconds(final Duration duration) {
        return String.format("%.1f", duration.toMillis() / 1000.0);
    }
}
