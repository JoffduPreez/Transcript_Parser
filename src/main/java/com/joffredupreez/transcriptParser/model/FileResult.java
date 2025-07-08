package com.joffredupreez.transcriptParser.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class FileResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to the user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Lob
    private String summary;

    @Lob
    private String transcript;

    private String originalFilename;

    private LocalDateTime uploadedAt;

    private List<String> actionItems = new ArrayList<>();

    private String fileDownloadPath;


}