package net.javaguides.reactive.ws.users.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
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
@Slf4j
public class UserController {

    @PostMapping
    public Mono<ResponseEntity<UserRest>> createUser(@RequestBody @Valid Mono<CreateUserRequest> createUserRequest) {
        log.info("🌐 - Create User Request called with data: {}", createUserRequest);

        // Simulate user creation logic (e.g., saving to database)
        return createUserRequest
                .map(request -> {
                    // Simulate user creation and return a UserRest object
                    UserRest user = UserRest.builder()
                            .id(java.util.UUID.randomUUID())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .email(request.getEmail())
                            .build();
                    log.info("✅✅ - User created successfully: {}", user);
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .location(URI.create("/api/v1/users/" + user.getId()))
                            .body(user);
                })
                .doOnError(error -> log.error("❌ - Error creating user: {}", error.getMessage()));
    }

    @GetMapping("/{userId}")
    public Mono<UserRest> getUser(@PathVariable("userId") final UUID userId) {
        log.info("🌐 - Get User Request called with id: {}", userId);

        // Simulate fetching user logic (e.g., retrieving from database)
        // For demonstration, we will just log the request and return a dummy user
        final UserRest user = UserRest.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@gmail.com")
                .build();

        log.info("✅✅ - User fetched successfully: {}", user);
        return Mono.just(user);
    }

}
