package com.official.lockr.domain.club.feed.domain.comment;

import java.util.ArrayList;
import java.util.List;

public class CommentHearts {

    private final List<CommentHeart> hearts;

    public CommentHearts() {
        this(new ArrayList<>());
    }

    public CommentHearts(final List<CommentHeart> hearts) {
        this.hearts = hearts;
    }

    public boolean isAlready(final String userId) {
        return hearts.stream()
                .anyMatch(heart -> heart.getUserId().equals(userId) && !heart.isDeleted());
    }

    public void add(final CommentHeart commentHeart) {
        this.hearts.add(commentHeart);
    }

    public void remove(final String userId) {
        hearts.stream()
                .filter(heart -> heart.getUserId().equals(userId) && !heart.isDeleted())
                .findFirst()
                .ifPresent(CommentHeart::delete);
    }

    public List<CommentHeart> getHearts() {
        return hearts.stream()
                .filter(heart -> !heart.isDeleted())
                .toList();
    }

    public int count() {
        return Math.toIntExact(hearts.stream()
                .filter(heart -> !heart.isDeleted())
                .count());
    }

    public void delete(final String userId) {
        hearts.stream()
                .filter(it -> it.isSameUser(userId))
                .forEach(CommentHeart::delete);
    }
}
