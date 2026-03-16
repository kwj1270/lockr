package com.official.lockr.domain.club.feed.api;

import com.official.lockr.domain.club.feed.domain.Feed;
import com.official.lockr.domain.club.feed.domain.FeedRepository;
import com.official.lockr.domain.club.schedule.domain.event.CancelledScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.CreatedScheduleEvent;
import com.official.lockr.domain.club.schedule.domain.event.UpdatedScheduleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

import static com.official.lockr.global.util.UlidUtils.generateUlid;

@Component
public class FeedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FeedEventConsumer.class);
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final FeedRepository feedRepository;
    private final RetryTemplate retryTemplate;

    public FeedEventConsumer(final FeedRepository feedRepository, final RetryTemplate retryTemplate) {
        this.feedRepository = feedRepository;
        this.retryTemplate = retryTemplate;
    }

    @TransactionalEventListener
    public void handleCreatedSchedule(final CreatedScheduleEvent event) {
        try {
            retryTemplate.execute(ctx -> {
                final String metadata = "{\"scheduleId\":\"" + event.scheduleId() + "\"}";
                final String content = buildScheduleContent(event.scheduleTime().format(DATETIME_FORMAT), event.location());

                final Feed feed = Feed.createFromSchedule(
                        generateUlid(),
                        event.clubId(),
                        event.userId(),
                        metadata,
                        event.title(),
                        content
                );

                feedRepository.save(feed);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to create feed from schedule after all retries. scheduleId={}, clubId={}",
                    event.scheduleId(), event.clubId(), e);
        }
    }

    @TransactionalEventListener
    public void handleUpdatedSchedule(final UpdatedScheduleEvent event) {
        try {
            retryTemplate.execute(ctx -> {
                final Feed feed = feedRepository.findByScheduleId(event.scheduleId());
                if (feed == null) {
                    log.warn("Feed not found for scheduleId={}. Skipping update.", event.scheduleId());
                    return null;
                }

                final String content = buildScheduleContent(event.newScheduleTime().format(DATETIME_FORMAT), event.location());
                feed.updateFromSchedule(event.title(), content);

                feedRepository.save(feed);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to update feed from schedule after all retries. scheduleId={}, clubId={}",
                    event.scheduleId(), event.clubId(), e);
        }
    }

    @TransactionalEventListener
    public void handleCancelledSchedule(final CancelledScheduleEvent event) {
        try {
            retryTemplate.execute(ctx -> {
                final Feed feed = feedRepository.findByScheduleId(event.scheduleId());
                if (feed == null) {
                    log.warn("Feed not found for scheduleId={}. Skipping delete.", event.scheduleId());
                    return null;
                }

                feed.deleteFromSchedule();
                feedRepository.save(feed);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to delete feed from schedule after all retries. scheduleId={}, clubId={}",
                    event.scheduleId(), event.clubId(), e);
        }
    }

    private static String buildScheduleContent(final String dateTime, final String location) {
        final StringBuilder sb = new StringBuilder();
        sb.append("일시: ").append(dateTime);
        if (location != null && !location.isBlank()) {
            sb.append("\n장소: ").append(location);
        }
        return sb.toString();
    }
}
