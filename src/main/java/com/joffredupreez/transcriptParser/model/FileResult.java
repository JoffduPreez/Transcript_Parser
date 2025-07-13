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

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getStoredFilePath() {
        return storedFilePath;
    }

    public void setStoredFilePath(String storedFilePath) {
        this.storedFilePath = storedFilePath;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<String> actionItems) {
        this.actionItems = actionItems;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getLastProcessedAt() {
        return lastProcessedAt;
    }

    public void setLastProcessedAt(LocalDateTime lastProcessedAt) {
        this.lastProcessedAt = lastProcessedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getTranscriptionJobId() {
        return transcriptionJobId;
    }

    public void setTranscriptionJobId(String transcriptionJobId) {
        this.transcriptionJobId = transcriptionJobId;
    }

    public String getSummarizationJobId() {
        return summarizationJobId;
    }

    public void setSummarizationJobId(String summarizationJobId) {
        this.summarizationJobId = summarizationJobId;
    }

    public String getTaskExtractionJobId() {
        return taskExtractionJobId;
    }

    public void setTaskExtractionJobId(String taskExtractionJobId) {
        this.taskExtractionJobId = taskExtractionJobId;
    }

}