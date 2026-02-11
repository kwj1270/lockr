package com.official.lockr.global.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(final String message) {
        super(message);
    }
}
