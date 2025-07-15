package com.joffredupreez.transcriptParser.service;

import com.joffredupreez.transcriptParser.controller.FileController;
import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.DTO;
import com.joffredupreez.transcriptParser.model.FileResult;
import com.joffredupreez.transcriptParser.model.ProcessingStatus;
import com.joffredupreez.transcriptParser.repositiory.FileResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Optional;

@Service
public class ProcessingService {

    @Autowired private FileResultRepository fileResultService;
    @Autowired private PythonServiceClient pythonClient;
    @Autowired private TaskScheduler taskScheduler;
    private static final Logger logger = LoggerFactory.getLogger(ProcessingService.class);

    public DTO.ProcessingResponse startProcessing(Long id, AppUser user) {
        FileResult fileResult = fileResultService.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("File not found or access denied"));

        // Check if already processing
        if (isCurrentlyProcessing(fileResult)) {
            throw new IllegalStateException("File is already being processed");
        }

        // Start the pipeline based on file type
        if (isAudioOrVideo(fileResult.getFileType())) {
            startTranscription(fileResult);
        } else if (fileResult.getFileType().equals("pdf")) {
            // TODO - Extract plain text from PDF

        } else {
            // Plain text file, skip transcription, go directly to summarization
            startSummarization(fileResult, null);
        }

        return new DTO.ProcessingResponse(id, "Processing started");
    }

    private void startTranscription(FileResult fileResult) {
        logger.info("Starting transcription for file: {}", fileResult.getId());

        try {
            // Call Python service
            TranscriptionJobResponse response = pythonClient.startTranscription(
                    fileResult.getStoredFilePath());

            // Update database
            fileResult.setTranscriptionJobId(response.getJobId());
            fileResult.setTranscriptionStatus(ProcessingStatus.PROCESSING);
            fileResult.setTranscriptionStartedAt(LocalDateTime.now());
            fileResultRepository.save(fileResult);

            // Schedule status check
            scheduleStatusCheck(fileResult.getId(), "transcription");

        } catch (Exception e) {
            logger.error("Failed to start transcription", e);
            fileResult.setTranscriptionStatus(ProcessingStatus.FAILED);
            fileResult.setErrorMessage("Transcription failed: " + e.getMessage());
            fileResultRepository.save(fileResult);
        }
    }

    private void startSummarization(FileResult fileResult, String transcriptText) {
        logger.info("Starting summarization for file: {}", fileResult.getId());

        try {
            String textToSummarize = transcriptText != null ? transcriptText :
                    readTextFromFile(fileResult.getStoredFilePath());

            SummarizationJobResponse response = pythonClient.startSummarization(textToSummarize);

            fileResult.setSummarizationJobId(response.getJobId());
            fileResult.setSummarizationStatus(ProcessingStatus.PROCESSING);
            fileResult.setSummarizationStartedAt(LocalDateTime.now());
            fileResultRepository.save(fileResult);

            scheduleStatusCheck(fileResult.getId(), "summarization");

        } catch (Exception e) {
            logger.error("Failed to start summarization", e);
            fileResult.setSummarizationStatus(ProcessingStatus.FAILED);
            fileResult.setErrorMessage("Summarization failed: " + e.getMessage());
            fileResultRepository.save(fileResult);
        }
    }

    private void startTaskExtraction(FileResult fileResult, String textContent) {
        logger.info("Starting task extraction for file: {}", fileResult.getId());

        try {
            TaskExtractionJobResponse response = pythonClient.startTaskExtraction(textContent);

            fileResult.setTaskExtractionJobId(response.getJobId());
            fileResult.setTaskExtractionStatus(ProcessingStatus.PROCESSING);
            fileResult.setTaskExtractionStartedAt(LocalDateTime.now());
            fileResultRepository.save(fileResult);

            scheduleStatusCheck(fileResult.getId(), "task-extraction");

        } catch (Exception e) {
            logger.error("Failed to start task extraction", e);
            fileResult.setTaskExtractionStatus(ProcessingStatus.FAILED);
            fileResult.setErrorMessage("Task extraction failed: " + e.getMessage());
            fileResultRepository.save(fileResult);
        }
    }

    @Async
    public void scheduleStatusCheck(Long fileId, String step) {
        // Check every 10 seconds, max 30 times (5 minutes)
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(10000); // 10 seconds

                if (checkAndUpdateStatus(fileId, step)) {
                    break; // Job completed or failed
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private boolean checkAndUpdateStatus(Long fileId, String step) {
        FileResult fileResult = fileResultRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        try {
            switch (step) {
                case "transcription":
                    return checkTranscriptionStatus(fileResult);
                case "summarization":
                    return checkSummarizationStatus(fileResult);
                case "task-extraction":
                    return checkTaskExtractionStatus(fileResult);
                default:
                    return true;
            }
        } catch (Exception e) {
            logger.error("Error checking status for step: {}", step, e);
            return true; // Stop checking on error
        }
    }

    private boolean checkTranscriptionStatus(FileResult fileResult) {
        JobStatusResponse status = pythonClient.checkJobStatus(fileResult.getTranscriptionJobId());

        if ("completed".equals(status.getStatus())) {
            fileResult.setTranscript(status.getResult());
            fileResult.setTranscriptionStatus(ProcessingStatus.COMPLETED);
            fileResultRepository.save(fileResult);

            // Start next step
            startSummarization(fileResult, status.getResult());
            return true;

        } else if ("failed".equals(status.getStatus())) {
            fileResult.setTranscriptionStatus(ProcessingStatus.FAILED);
            fileResult.setErrorMessage("Transcription failed: " + status.getError());
            fileResultRepository.save(fileResult);
            return true;
        }

        return false; // Still processing
    }

    private boolean checkSummarizationStatus(FileResult fileResult) {
        JobStatusResponse status = pythonClient.checkJobStatus(fileResult.getSummarizationJobId());

        if ("completed".equals(status.getStatus())) {
            fileResult.setSummary(status.getResult());
            fileResult.setSummarizationStatus(ProcessingStatus.COMPLETED);
            fileResultRepository.save(fileResult);

            // Start task extraction
            String textForTasks = fileResult.getTranscript() != null ?
                    fileResult.getTranscript() : readTextFromFile(fileResult.getStoredFilePath());
            startTaskExtraction(fileResult, textForTasks);
            return true;

        } else if ("failed".equals(status.getStatus())) {
            fileResult.setSummarizationStatus(ProcessingStatus.FAILED);
            fileResult.setErrorMessage("Summarization failed: " + status.getError());
            fileResultRepository.save(fileResult);
            return true;
        }

        return false;
    }

    private boolean checkTaskExtractionStatus(FileResult fileResult) {
        JobStatusResponse status = pythonClient.checkJobStatus(fileResult.getTaskExtractionJobId());

        if ("completed".equals(status.getStatus())) {
            fileResult.setExtractedTasks(status.getResult());
            fileResult.setTaskExtractionStatus(ProcessingStatus.COMPLETED);
            fileResult.setCompletedAt(LocalDateTime.now());
            fileResultRepository.save(fileResult);
            return true;

        } else if ("failed".equals(status.getStatus())) {
            fileResult.setTaskExtractionStatus(ProcessingStatus.FAILED);
            fileResult.setErrorMessage("Task extraction failed: " + status.getError());
            fileResultRepository.save(fileResult);
            return true;
        }

        return false;
    }

    public ProcessingStatus getProcessingStatus(Long id, AppUser user) {
        FileResult fileResult = fileResultService.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("File not found or access denied"));

        return fileResult.getStatus();
    }

    // Helper methods
    private boolean isCurrentlyProcessing(FileResult fileResult) {
        return fileResult.getStatus() == ProcessingStatus.TRANSCRIBING ||
                fileResult.getStatus() == ProcessingStatus.SUMMARIZING ||
                fileResult.getStatus() == ProcessingStatus.EXTRACTING;
    }

    private boolean isAudioOrVideo(String fileType) {
        return fileType.equals("audio") || fileType.equals("video");
    }

    private String readTextFromFile(String filePath) {
        // Implementation to read text files
        // Return file content as string
        return ""; // Placeholder
    }
}