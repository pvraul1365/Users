package net.javaguides.reactive.ws.users.service;

import java.util.UUID;
import net.javaguides.reactive.ws.users.presentation.CreateUserRequest;
import net.javaguides.reactive.ws.users.presentation.UserRest;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono);

    Mono<UserRest> getUserById(UUID id);
}
