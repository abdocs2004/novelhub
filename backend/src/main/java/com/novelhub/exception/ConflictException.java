package com.novelhub.exception;

/** Mapped to HTTP 409 (duplicate email, already purchased, duplicate chapter number ...). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
