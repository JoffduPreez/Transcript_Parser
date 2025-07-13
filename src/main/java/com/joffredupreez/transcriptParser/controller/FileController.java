package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.FileResult;
import com.joffredupreez.transcriptParser.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileResult> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            // Validate file
            validateFile(file);

            // Get current user
            String username = authentication.getName();

            // Save file and create FileResult
            FileResult fileResult = fileStorageService.storeFile(file, username);

            return ResponseEntity.ok(fileResult); // TODO - might change this idk yet

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private void validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file size (e.g., max 500MB)
        // TODO - might change max file size
        if (file.getSize() > 500 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large");
        }

        // Check file type
        String contentType = file.getContentType();
        if (!isValidFileType(contentType)) {
            throw new IllegalArgumentException("Invalid file type");
        }
    }

    private boolean isValidFileType(String contentType) {
        return contentType != null && (
                contentType.startsWith("audio/") ||
                        contentType.startsWith("video/") ||
                        contentType.equals("text/plain") ||
                        contentType.equals("application/pdf")
        );
    }
}