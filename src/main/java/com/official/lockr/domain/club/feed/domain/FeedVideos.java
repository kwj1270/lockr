package com.official.lockr.domain.club.feed.domain;

import com.official.lockr.domain.club.feed.domain.entity.Video;
import com.official.lockr.global.util.UlidUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeedVideos {

    private final String feedId;
    private final List<Video> videos;

    public FeedVideos(final String feedId, final List<Video> videos) {
        this.feedId = feedId;
        this.videos = videos;
    }

    public static FeedVideos from(final List<String> urls, final String userId, final String feedId) {
        if (urls == null || urls.isEmpty()) {
            return new FeedVideos(feedId, Collections.emptyList());
        }
        final List<Video> videos = urls.stream()
                .map(url -> new Video(UlidUtils.generateUlid(), url, userId, LocalDateTime.now(), null))
                .collect(Collectors.toCollection(ArrayList::new));
        return new FeedVideos(feedId, videos);
    }

    public static FeedVideos empty(final String feedId) {
        return new FeedVideos(feedId, Collections.emptyList());
    }

    public List<Video> getVideos() {
        return Collections.unmodifiableList(videos);
    }

    public List<Video> getActiveVideos() {
        return videos.stream()
                .filter(video -> !video.isDeleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> getUrls() {
        return videos.stream()
                .filter(video -> !video.isDeleted())
                .map(Video::getUrl)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int count() {
        return (int) videos.stream()
                .filter(video -> !video.isDeleted())
                .count();
    }

    public boolean isEmpty() {
        return videos.stream().allMatch(Video::isDeleted);
    }

    public String getFeedId() {
        return feedId;
    }
}
