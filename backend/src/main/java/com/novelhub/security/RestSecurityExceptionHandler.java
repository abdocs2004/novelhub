package com.novelhub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelhub.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security errors happen INSIDE the filter chain, before Spring MVC, so @RestControllerAdvice cannot catch them.
 * These two callbacks make Spring Security answer with the same JSON error format as the rest of the API.
 *  - 401: no/invalid token (you are not logged in)
 *  - 403: logged in, but not allowed (e.g. a USER calling /api/admin/**)
 */
@Component
public class RestSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestSecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(response, HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource");
    }

    private void write(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(status.value(), code, message, null));
    }
}
