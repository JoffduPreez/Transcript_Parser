package com.joffredupreez.transcriptParser.service;

import com.joffredupreez.transcriptParser.model.AppUser;
import com.joffredupreez.transcriptParser.repositiory.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        /*
        Here, we return the user data as a Spring Security UserDetails object
        Spring Security uses this UserDetails object to compare the
        password the user typed with the password stored in the database (via the PasswordEncoder).
        */
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>() // list of roles/authorities (empty for now)
        );
    }
}
