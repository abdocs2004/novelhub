package com.novelhub.dto;

import java.time.Instant;
import java.util.Map;

/** The JSON shape of every error returned by the API. */
public record ApiError(Instant timestamp, int status, String code, String message, Map<String, String> fieldErrors) {

    public static ApiError of(int status, String code, String message, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, code, message, fieldErrors);
    }
}
