package com.official.lockr.domain.club.feed.domain;

import com.official.lockr.domain.club.feed.domain.entity.Image;
import com.official.lockr.global.util.UlidUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeedImages {

    private final String feedId;
    private final List<Image> images;

    public FeedImages(final String feedId, final List<Image> images) {
        this.feedId = feedId;
        this.images = images;
    }

    public static FeedImages from(final List<String> urls, final String userId, final String feedId) {
        if (urls == null || urls.isEmpty()) {
            return new FeedImages(feedId, Collections.emptyList());
        }
        final List<Image> images = urls.stream()
                .map(url -> new Image(UlidUtils.generateUlid(), url, userId, LocalDateTime.now(), null))
                .collect(Collectors.toCollection(ArrayList::new));
        return new FeedImages(feedId, images);
    }

    public static FeedImages empty(final String feedId) {
        return new FeedImages(feedId, Collections.emptyList());
    }

    public List<Image> getImages() {
        return Collections.unmodifiableList(images);
    }

    public List<Image> getActiveImages() {
        return images.stream()
                .filter(image -> !image.isDeleted())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> getUrls() {
        return images.stream()
                .filter(image -> !image.isDeleted())
                .map(Image::getUrl)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int count() {
        return (int) images.stream()
                .filter(image -> !image.isDeleted())
                .count();
    }

    public boolean isEmpty() {
        return images.stream().allMatch(Image::isDeleted);
    }

    public String getFeedId() {
        return feedId;
    }
}
