package com.joffredupreez.transcriptParser.model;

public enum ProcessingStatus {
    UPLOADED,        // File just uploaded, waiting to start
    TRANSCRIBING,    // Currently converting audio to text
    SUMMARIZING,     // Currently generating summary
    EXTRACTING,      // Currently extracting tasks/entities
    COMPLETED,       // All processing finished successfully
    FAILED           // Something went wrong
}
