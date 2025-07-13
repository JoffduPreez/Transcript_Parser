package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.FileResult;
import com.joffredupreez.transcriptParser.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            // Validate file
            fileStorageService.validateFile(file);

            // Get current user
            String username = authentication.getName();

            // Save file and create FileResult
            FileResult fileResult = fileStorageService.storeFile(file, username);

            return ResponseEntity.ok(fileResult);

        } catch (IllegalArgumentException e) {
            // Client error - bad request

            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (IOException e) {
            // Server error - storage issue
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "File storage failed"));

        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error occurred"));
        }
    }




}