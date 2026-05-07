package net.javaguides.reactive.ws.users.service;

import reactor.core.publisher.Mono;

/**
 * Created by ij, Spring Framework Guru.
 *
 * @author architecture - raulp
 * @version 07/05/2026 - 14:55
 * @since jdk 1.21
 */
public interface JwtService {

    String generateToken(String subject); // userId

    Mono<Boolean> validateJwt(String token);

    String extractTokenSubject(String token);
}
