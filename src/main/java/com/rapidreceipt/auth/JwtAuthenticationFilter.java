package com.rapidreceipt.auth;

import com.rapidreceipt.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every incoming HTTP request and validates the JWT token if present.
 *
 * How it works:
 * 1. Check for the Authorization header with a "Bearer " prefix.
 * 2. Extract the email from the token subject.
 * 3. Load the User from the DB to get their current details.
 * 4. If the token is valid, set the authentication in the SecurityContext.
 * 5. Spring Security reads the SecurityContext to decide if the request is allowed.
 *
 * OncePerRequestFilter guarantees this runs exactly once per request,
 * even in complex dispatch scenarios (forwards, includes).
 *
 * If no token is present or the token is invalid, the filter simply lets the
 * request continue — Spring Security's authorization logic will then reject it
 * with 401/403 if the endpoint requires authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No Authorization header or not a Bearer token — skip JWT processing
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token (everything after "Bearer ")
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Malformed or tampered token — let the request proceed unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        // Only proceed if we got an email AND no authentication is already set
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userRepository.findByEmail(userEmail).orElse(null);

            if (userDetails != null && jwtService.isTokenValid(jwt, userDetails)) {
                // Create the auth token that Spring Security understands
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                           // credentials null for JWT (no password needed after initial auth)
                                userDetails.getAuthorities()
                        );
                // Attach request metadata (IP, session info) to the token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Tell Spring Security this request is authenticated
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
