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

    // File metadata
    private String originalFilename;
    private String fileType; // "audio", "video", "text", "transcript"
    private Long fileSizeBytes;
    private String storedFilePath;
    private LocalDateTime uploadedAt;

    // Processing status
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status; // UPLOADED, TRANSCRIBING, SUMMARIZING, COMPLETED, FAILED

    // Processing results
    @Lob
    private String transcript;
    @Lob
    private String summary;

    @ElementCollection
    @CollectionTable(name = "file_result_action_items")
    private List<String> actionItems = new ArrayList<>();

    // Error handling
    private String errorMessage;
    private LocalDateTime lastProcessedAt;
    private LocalDateTime completedAt;

    /*
    Job tracking - these store unique identifiers for each processing step.
    1. User uploads file
    2. Start transcription
    3. Check status periodically (or when user refreshes page) (or get webhook)
    4. Start next step (summarization)
    5. Repeat for each step

    Python responds with a job ID:
    { "jobId": "transcribe-12345", "status": "processing" }

    You store this ID:
    fileResult.setTranscriptionJobId("transcribe-12345");

    Later, you can check status:
    GET /python-service/jobs/transcribe-12345
    Response: { "status": "completed", "transcript": "..." }
     */
    private String transcriptionJobId;
    private String summarizationJobId; 
    private String taskExtractionJobId;
    
}