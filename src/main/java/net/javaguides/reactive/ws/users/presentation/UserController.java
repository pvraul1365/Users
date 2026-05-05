package net.javaguides.reactive.ws.users.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.reactive.ws.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * UserController
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 04/05/2026 - 09:50
 * @since 1.17
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public Mono<ResponseEntity<UserRest>> createUser(@RequestBody @Valid Mono<CreateUserRequest> createUserRequest) {
        log.info("🌐 - Create User Request called with data: {}", createUserRequest);

        return userService.createUser(createUserRequest)
                .map(userRest -> {
                    log.info("✅ - User created successfully: {}", userRest);
                    return ResponseEntity.created(URI.create("/api/v1/users/" + userRest.getId())).body(userRest);
                })
                .doOnError(error -> log.error("❌ - Error creating user: {}", error.getMessage()));
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserRest>> getUser(@PathVariable("userId") final UUID userId) {
        log.info("🌐 - Get User Request called with id: {}", userId);

        return userService.getUserById(userId)
                .map(userRest -> {
                    log.info("✅ - User retrieved successfully: {}", userRest);
                    return ResponseEntity.ok(userRest);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @GetMapping
    public Flux<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") final int page,
                                   @RequestParam(value = "limit", defaultValue = "50") final int limit) {
        log.info("🌐 - Get Users Request called");

        // Simulate fetching all users logic (e.g., retrieving from database)
        // For demonstration, we will just log the request
        return userService.findAll(page, limit)
                .doOnNext(user -> log.info("📋 - User: {}", user))
                .doOnComplete(() -> log.info("✅ - All users fetched successfully"))
                .doOnError(error -> log.error("❌ - Error fetching users: {}", error.getMessage()));
    }
}
