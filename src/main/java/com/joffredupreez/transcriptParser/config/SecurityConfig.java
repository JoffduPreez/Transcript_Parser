package com.joffredupreez.transcriptParser.config;

import com.joffredupreez.transcriptParser.service.JPAUserDetailsService;
import com.joffredupreez.transcriptParser.service.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private JwtFilter jwtFilter;
    @Autowired private JPAUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable  CSRF protection. This is okay in stateless APIs where you're not using cookies or forms
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow open access to /auth/** (e.g., login/register), and require authentication for everything else.
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                        // Sets the app to stateless. You're telling Spring not to create sessions — instead, each request must contain a valid JWT.
                ).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Injects your custom JWT filter into the filter chain, before Spring's built-in username/password filter.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Provide a configured AuthenticationManager as a Spring Bean so that your login controller or auth filter can use it to authenticate users.
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        // Retrieve the builder used to configure authentication rules.
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        // Tell Spring how to load a user by username — it’ll use CustomUserDetailsService.
        // Ensure password comparisons use my defined encoder (usually BCrypt).
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
