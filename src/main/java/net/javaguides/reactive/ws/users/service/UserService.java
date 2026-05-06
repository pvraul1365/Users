package net.javaguides.reactive.ws.users.service;

import java.util.UUID;
import net.javaguides.reactive.ws.users.presentation.model.CreateUserRequest;
import net.javaguides.reactive.ws.users.presentation.model.UserRest;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService extends ReactiveUserDetailsService {

    Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono);

    Mono<UserRest> getUserById(UUID id);

    Flux<UserRest> findAll(int page, int size);
}
