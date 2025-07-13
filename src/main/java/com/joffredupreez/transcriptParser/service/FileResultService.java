package com.joffredupreez.transcriptParser.service;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.model.FileResult;
import com.joffredupreez.transcriptParser.repositiory.FileResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileResultService {

    @Autowired
    private FileResultRepository fileResultRepository;

    public FileResult save(FileResult fileResult) {
        return fileResultRepository.save(fileResult);
    }

    public List<FileResult> findByUser(AppUser user) {
        return fileResultRepository.findByUserOrderByUploadedAtDesc(user);
    }

    public Optional<FileResult> findById(Long id) {
        return fileResultRepository.findById(id);
    }

    public Optional<FileResult> findByIdAndUser(Long id, AppUser user) {
        return fileResultRepository.findByIdAndUser(id, user);
    }

    public void deleteById(Long id) {
        fileResultRepository.deleteById(id);
    }
}