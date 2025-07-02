package com.joffredupreez.transcriptParser;

import com.joffredupreez.transcriptParser.repositiory.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TranscriptParserApplication {

    @Autowired
    private AppUserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(TranscriptParserApplication.class, args);
    }
}
