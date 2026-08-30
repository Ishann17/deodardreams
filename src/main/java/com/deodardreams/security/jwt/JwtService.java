package com.deodardreams.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // Builds the actual cryptographic key object jjwt needs, from our raw secret string.
    private SecretKey getSigningKey() {
        //this converts your raw secret string into the specific key object format the signing algorithm needs internally.
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Creates a signed token for the given admin — email as the subject, role as a custom claim.
    public String generateToken(String email, String role) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // Parses and verifies the token's signature, then extracts all claims from it.
    // Throws JwtException if the signature is invalid or the token is malformed —
    // this is the actual moment tampering or forgery would be detected.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extracts the admin's email from the token's subject.
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extracts the role claim we stored during token creation.
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // Checks whether the token is both correctly signed AND not yet expired.
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
