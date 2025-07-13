package com.joffredupreez.transcriptParser.repositiory;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.FileResult;
import com.joffredupreez.transcriptParser.model.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileResultRepository extends JpaRepository<FileResult, Long> {
    // Finds all files for a specific user, sorted by upload date (newest first)
    List<FileResult> findByUserOrderByUploadedAtDesc(AppUser user);

    // Finds a file by ID, but only if it belongs to the specified user (security!)
    Optional<FileResult> findByIdAndUser(Long id, AppUser user);

    // Finds all files for a user with a specific processing status
    List<FileResult> findByUserAndStatus(AppUser user, ProcessingStatus status);
}