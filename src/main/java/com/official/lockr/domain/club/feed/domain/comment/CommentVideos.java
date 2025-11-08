package com.official.lockr.domain.club.feed.domain.comment;

import com.official.lockr.domain.club.feed.domain.entity.Video;
import com.official.lockr.global.util.UlidUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommentVideos {

    private final String commentId;
    private final List<Video> videos;

    public CommentVideos(final String commentId, final List<Video> videos) {
        this.commentId = commentId;
        this.videos = videos;
    }

    public static CommentVideos from(final List<String> urls, final String userId, final String commentId) {
        if (urls == null || urls.isEmpty()) {
            return new CommentVideos(commentId, Collections.emptyList());
        }
        final List<Video> videos = urls.stream()
                .map(url -> new Video(UlidUtils.generateUlid(), url, userId, LocalDateTime.now(), null))
                .collect(Collectors.toCollection(ArrayList::new));
        return new CommentVideos(commentId, videos);
    }

    public static CommentVideos empty(final String commentId) {
        return new CommentVideos(commentId, Collections.emptyList());
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

    public String getCommentId() {
        return commentId;
    }
}
