package com.joffredupreez.transcriptParser.controller;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.DTO;
import com.joffredupreez.transcriptParser.model.ProcessingStatus;
import com.joffredupreez.transcriptParser.service.ProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/processing")
public class ProcessingController {

    @Autowired
    private ProcessingService processingService;

    @PostMapping("/process/{id}")
    public ResponseEntity<DTO.ProcessingResponse> startProcessing(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {

        DTO.ProcessingResponse response = processingService.startProcessing(id, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ProcessingStatus> getStatus(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        
        ProcessingStatus status = processingService.getProcessingStatus(id, user);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<ProcessingJobSummary>> getRecentJobs(
            @AuthenticationPrincipal AppUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ProcessingJobSummary> jobs = processingService.getRecentJobs(user.getUsername(), page, size);
        return ResponseEntity.ok(jobs);
    }
}