package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.DTO;
import com.joffredupreez.transcriptParser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(new DTO.ProfileResponse(user.getUsername(), user.getEmail()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@AuthenticationPrincipal AppUser user, @RequestBody DTO.UpdateProfileRequest updateRequest) {
        user.setUsername(updateRequest.username);
        user.setEmail(updateRequest.email);

        if (userService.save(user) != null) {
            return ResponseEntity.ok(new DTO.ProfileResponse(user.getUsername(), user.getEmail()));
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

}