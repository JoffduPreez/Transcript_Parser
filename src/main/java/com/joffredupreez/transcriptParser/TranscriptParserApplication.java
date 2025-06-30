package com.joffredupreez.transcriptParser;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.repositiory.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TranscriptParserApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(TranscriptParserApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Save dummy User
        AppUser user = new AppUser("joffre", "joffre@example.com");
        userRepository.save(user);

        System.out.println("Dummy meeting and user inserted into DB.");
    }
}
