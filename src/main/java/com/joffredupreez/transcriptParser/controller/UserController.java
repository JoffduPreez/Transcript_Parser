package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal AppUser user) {
        logger.info("user: " + user.getUsername());
        // user is already the logged-in AppUser entity
        return ResponseEntity.ok(user);
    }
}