package com.rapidreceipt.config;

import com.rapidreceipt.auth.JwtAuthenticationFilter;
import com.rapidreceipt.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * Key decisions:
 * - CSRF disabled — this is a stateless REST API. CSRF protection is only
 *   needed for browser-based apps using cookie sessions.
 * - SessionCreationPolicy.STATELESS — Spring Security will never create an
 *   HttpSession. Every request must carry its own JWT.
 * - /api/auth/** is public — everything else requires a valid JWT.
 * - BCrypt strength 10 (default) — good balance of security and performance.
 * - UserDetailsService is defined as a lambda here, loading User by email.
 *   User implements UserDetails so no wrapper class is needed.
 * - JwtAuthenticationFilter runs BEFORE Spring's default
 *   UsernamePasswordAuthenticationFilter so our token check happens first.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()    // register + login are public
                .anyRequest().authenticated()                   // everything else needs a JWT
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Loads User by email from the DB.
     * User implements UserDetails so we return it directly — no wrapper needed.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    /**
     * Wires together: UserDetailsService (how to load users) +
     * PasswordEncoder (how to verify passwords).
     * Spring Security's DaoAuthenticationProvider uses both during login.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager as a bean so AuthService can
     * call authenticate() to validate login credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder — strength 10 by default.
     * AuthService uses this to hash passwords on register.
     * DaoAuthenticationProvider uses it to verify on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
