package com.novelhub.config;

import com.novelhub.security.CustomUserDetailsService;
import com.novelhub.security.JwtAuthFilter;
import com.novelhub.security.JwtService;
import com.novelhub.security.RestSecurityExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RestSecurityExceptionHandler securityExceptionHandler;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtService jwtService,
                          CustomUserDetailsService userDetailsService,
                          RestSecurityExceptionHandler securityExceptionHandler,
                          @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.securityExceptionHandler = securityExceptionHandler;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList();
    }

    /**
     * The security "rules". Every request goes through this chain of filters before reaching a controller.
     * Rules are checked top to bottom: the first matching rule wins.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protects cookie-based sessions. We use a token in a header and no cookies, so we disable it.
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // uses the CorsConfigurationSource bean below
                // STATELESS: the server keeps no session. Every request must carry its own JWT.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()          // CORS preflight
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/novels", "/api/novels/**").permitAll() // browse without login
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")               // ADMIN only
                        .anyRequest().authenticated())                                   // everything else: must be logged in
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler))
                // Our JWT filter must run before Spring's default username/password filter.
                .addFilterBefore(new JwtAuthFilter(jwtService, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt = slow, salted hashing designed for passwords. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * The AuthenticationManager checks email + password at login. Spring wires it with our
     * CustomUserDetailsService and PasswordEncoder automatically (they are the only ones in the context).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Lets the React dev server (another origin, e.g. localhost:5173) call this API from the browser. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
