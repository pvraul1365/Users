package net.javaguides.reactive.ws.users.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/**
 * Created by ij, Spring Framework Guru.
 *
 * @author architecture - rperezv
 * @version 07/05/2026 - 14:58
 * @since jdk 1.21
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final Environment environment;

    @Override
    public String generateToken(String subject) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(this.getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Optional.ofNullable(environment.getProperty("token.secret"))
                .map(tokenSecret -> tokenSecret.getBytes())
                .map(tokenSecretBytes -> Keys.hmacShaKeyFor(tokenSecretBytes))
                .orElseThrow(() -> new IllegalArgumentException("token.secret must be configured in the application properties"));
    }
}
