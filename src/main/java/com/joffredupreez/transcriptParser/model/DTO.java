package com.joffredupreez.transcriptParser.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

public class DTO {
    @AllArgsConstructor
    public static class ProfileResponse {
        public String username;
        public String email;
    }

    public static class UpdateProfileRequest {
        public String username;
        public String email;
    }

    @AllArgsConstructor
    public static class ProcessingResponse {
        public Long fileId;
        public String message;
    }

    public static class ProcessingStatusResponse {
        public Long fileId;
        public ProcessingStatus status;
        public String errorMessage;
    }
}
