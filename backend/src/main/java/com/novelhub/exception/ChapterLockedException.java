package com.novelhub.exception;

/** Thrown when a user asks for a paid chapter they did not buy. Mapped to HTTP 403. */
public class ChapterLockedException extends RuntimeException {
    public ChapterLockedException(String message) {
        super(message);
    }
}
