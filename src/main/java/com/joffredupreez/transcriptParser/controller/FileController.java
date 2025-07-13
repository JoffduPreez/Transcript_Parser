package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.FileResult;
import com.joffredupreez.transcriptParser.service.FileResultService;
import com.joffredupreez.transcriptParser.service.FileStorageService;
import com.joffredupreez.transcriptParser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired private FileStorageService fileStorageService;
    @Autowired private UserService userService;
    @Autowired private FileResultService fileResultService;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @GetMapping
    public ResponseEntity<List<FileResult>> getUserFiles(@AuthenticationPrincipal AppUser user) {
        List<FileResult> files = fileResultService.findByUser(user);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileResult> getFile(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        // This is either:
        // - Optional.of(someFileResult) if found
        // - Optional.empty() if not found
        Optional<FileResult> fileResult = fileResultService.findByIdAndUser(id, user);

        // map to OK response, or return 404 (logic behind this is confusing)
        return fileResult.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        Optional<FileResult> fileResult = fileResultService.findByIdAndUser(id, user);

        if (fileResult.isPresent()) {
            // Delete physical file
            try {
                Files.deleteIfExists(Paths.get(fileResult.get().getStoredFilePath()));
            } catch (IOException e) {
                // Log error but continue with database deletion
                logger.error("Failed to delete local file: {} for user: {}",
                        fileResult.get().getStoredFilePath(), user.getUsername(), e);
            }

            // Delete database record
            fileResultService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

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