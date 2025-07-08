package com.joffredupreez.transcriptParser.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired private JPAUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Look for the Authorization header.
        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String token = null;

        // Extract the JWT token (if present).
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtUtil.extractUsername(token);
        }

        // if no user is currently authenticated in this request's security context. This check ensures you don’t overwrite an existing authenticated user.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the user from the database using UserDetailsService.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the token
            if (jwtUtil.validateToken(token, userDetails)) {
                // Create a UsernamePasswordAuthenticationToken.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Set the user into Spring’s SecurityContextHolder, which marks the request as authenticated.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Pass the request and response along to the next filter in the chain (or the target servlet/controller if no more filters
        filterChain.doFilter(request, response);
    }
}
