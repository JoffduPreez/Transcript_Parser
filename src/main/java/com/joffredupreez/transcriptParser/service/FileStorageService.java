package com.joffredupreez.transcriptParser.service;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.FileResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // :uploads - Default value if property doesn't exist
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private UserService userService;

    @Autowired
    private FileResultService fileResultService;

    @PostConstruct
    public void init() {
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public FileResult storeFile(MultipartFile file, String username) throws IOException {
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Create user directory
        Path userDir = Paths.get(uploadDir, username);
        Files.createDirectories(userDir);

        // Save file
        Path filePath = userDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create FileResult
        AppUser user = userService.findByUsername(username);
        FileResult fileResult = new FileResult();
        fileResult.setUser(user);
        fileResult.setOriginalFilename(originalFilename);
        fileResult.setStoredFilePath(filePath.toString());
        fileResult.setFileType(determineFileType(file.getContentType()));
        fileResult.setFileSizeBytes(file.getSize());
        fileResult.setUploadedAt(LocalDateTime.now());
        fileResult.setStatus(ProcessingStatus.UPLOADED);

        return fileResultService.save(fileResult);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String determineFileType(String contentType) {
        if (contentType.startsWith("audio/")) return "audio";
        if (contentType.startsWith("video/")) return "video";
        if (contentType.equals("text/plain")) return "text";
        return "unknown";
    }
}