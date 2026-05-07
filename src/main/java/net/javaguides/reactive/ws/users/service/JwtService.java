package net.javaguides.reactive.ws.users.service;

/**
 * Created by ij, Spring Framework Guru.
 *
 * @author architecture - raulp
 * @version 07/05/2026 - 14:55
 * @since jdk 1.21
 */
public interface JwtService {

    String generateToken(String subject); // userId

}
