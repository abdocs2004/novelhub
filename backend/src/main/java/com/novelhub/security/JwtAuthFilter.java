package com.novelhub.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once for EVERY request, before the controller.
 * If the request has "Authorization: Bearer <token>" and the token is valid, it puts the user into
 * the SecurityContext ("this request is authenticated as X"). If not, the request simply stays anonymous
 * and Spring Security's authorization rules (SecurityConfig) decide whether that is allowed.
 *
 * Note: it is created with "new" inside SecurityConfig and is NOT a @Component, so Spring Boot does not
 * register it a second time as a plain servlet filter.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String email = jwtService.extractEmail(token);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Reload the user from the DB so role changes / deleted users are respected immediately.
                    UserDetails user = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
                // Invalid/expired token or deleted user: continue as anonymous. Protected endpoints will answer 401.
                logger.debug("Ignoring invalid JWT: " + ex.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
