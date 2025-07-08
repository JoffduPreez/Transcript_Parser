package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.repositiory.AppUserRepository;
import com.joffredupreez.transcriptParser.service.JPAUserDetailsService;
import com.joffredupreez.transcriptParser.service.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AuthenticationManager authManager;
    @Autowired private JPAUserDetailsService userDetailsService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder encoder;
    @Autowired private AppUserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /*
    Spring Boot will automatically convert (deserialize) a JSON request body into a Java object if you annotate it with @RequestBody.
    ResponseEntity<T> is a flexible way in Spring to build an HTTP response
    */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user) {
        // Hash the password before saving
        user.setPassword(encoder.encode(user.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username, request.password)
            );
            // Set the currently authenticated user into the Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Authentication failed, handle the exception (e.g., return an error message)
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.username);
        final String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // DTOs
    public static class AuthRequest {
        public String username;
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public AuthResponse(String token) { this.token = token; }
    }
}
