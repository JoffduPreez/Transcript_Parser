package com.joffredupreez.transcriptParser.config;

import com.joffredupreez.transcriptParser.service.CustomUserDetailsService;
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
    @Autowired private CustomUserDetailsService userDetailsService;

    /*
    Disables CSRF protection. This is okay in stateless APIs where you're not using cookies or forms
    Allowing open access to /auth/** (e.g., login/register), and requiring authentication for everything else.
    Sets the app to stateless. You're telling Spring not to create sessions — instead, each request must contain a valid JWT.
    Injects your custom JWT filter into the filter chain, before Spring's built-in username/password filter.
    This filter will extract the token, validate it, and set the authenticated user.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                ).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /*
    Provides a configured AuthenticationManager as a Spring Bean so that your login controller or auth filter can use it to authenticate users.

    Retrieves the builder used to configure authentication rules.
    Tells Spring how to load a user by username — it’ll use CustomUserDetailsService.
    Ensures password comparisons use your defined encoder (usually BCrypt).
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
