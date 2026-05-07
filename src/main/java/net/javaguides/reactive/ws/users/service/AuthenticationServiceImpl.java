package net.javaguides.reactive.ws.users.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.reactive.ws.users.data.UserEntity;
import net.javaguides.reactive.ws.users.data.UserRepository;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * AuthenticationServiceImpl
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 06/05/2026 - 11:31
 * @since 1.17
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public Mono<Map<String, String>> authenticate(final String username, final String password) {
        log.info("🔒 - Authenticating username {}", username);

        return reactiveAuthenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password))
                .then(getUserDetails(username)
                        .map(this::createAuthResponse)
                        .doOnSuccess(authResponse -> log.info("✅ - Authentication successful for username {}: {}", username, authResponse)))
                .doOnError(error -> log.error("❌ - Authentication failed for username {}: {}", username, error.getMessage()));
    }

    private Mono<UserEntity> getUserDetails(final String username) {
        return userRepository.findByEmail(username);
    }

    private Map<String, String> createAuthResponse(UserEntity userEntity) {
        Map<String, String> authResponse = new HashMap<>();
        authResponse.put("userId", userEntity.getId().toString());
        authResponse.put("token", jwtService.generateToken(userEntity.getId().toString()));

        return authResponse;
    }
}
