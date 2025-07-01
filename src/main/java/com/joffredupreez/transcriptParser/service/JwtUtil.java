package com.joffredupreez.transcriptParser.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
     @Value("${jwt.secret}")
     private String SECRET_KEY;

    // done once after login and sent to the user.
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername()) // The "username" or identifier for the user
                .setIssuedAt(new Date())               // When the token was created
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // When the token should expire (forces re-login)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Uses HMAC + SHA256 to cryptographically sign the token
                .compact(); // Finalizes and creates the token string
    }

    // done every time a request comes in, so Spring knows who is trying to access an endpoint.
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) // verify signature
                .parseClaimsJws(token)     // parse the token
                .getBody()                 // get the claims (payload)
                .getSubject();             // pull out the "sub" field (username)
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername());
    }
}
