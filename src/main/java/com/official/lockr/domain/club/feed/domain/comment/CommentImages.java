package com.official.lockr.domain.club.feed.domain.comment;

import com.official.lockr.domain.club.feed.domain.entity.Image;
import com.official.lockr.global.util.UlidUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommentImages {

    private final String commentId;
    private final List<Image> images;

    public CommentImages(final String commentId, final List<Image> images) {
        this.commentId = commentId;
        this.images = images;
    }

    public static CommentImages from(final List<String> urls, final String userId, final String commentId) {
        if (urls == null || urls.isEmpty()) {
            return new CommentImages(commentId, Collections.emptyList());
        }
        final List<Image> images = urls.stream()
                .map(url -> new Image(UlidUtils.generateUlid(), url, userId, LocalDateTime.now(), null))
                .collect(Collectors.toCollection(ArrayList::new));
        return new CommentImages(commentId, images);
    }

    public static CommentImages empty(final String commentId) {
        return new CommentImages(commentId, Collections.emptyList());
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

    public String getCommentId() {
        return commentId;
    }
}
