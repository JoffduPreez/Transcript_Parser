package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(new ProfileResponse(user.getUsername(), user.getEmail()));
    }

    public static class ProfileResponse {
        public String username;
        public String email;

        public ProfileResponse(String username, String email) {
            this.username = username;
            this.email = email;
        }
    }
}