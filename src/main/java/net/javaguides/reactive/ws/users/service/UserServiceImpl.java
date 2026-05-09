package net.javaguides.reactive.ws.users.service;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.reactive.ws.users.data.UserEntity;
import net.javaguides.reactive.ws.users.data.UserRepository;
import net.javaguides.reactive.ws.users.presentation.model.CreateUserRequest;
import net.javaguides.reactive.ws.users.presentation.model.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * UserServiceImpl
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 04/05/2026 - 15:30
 * @since 1.17
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Sinks.Many<UserRest> usersSink;

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        log.info("📥 - UserServiceImpl.createUser called with data: {}", createUserRequestMono);

        return createUserRequestMono
                .flatMap(this::convertToEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToRest)
                .doOnSuccess(saveUser -> {
                    log.info("✅ - User created successfully: {}", saveUser);
                    usersSink.tryEmitNext(saveUser);
                });
    }

    @Override
    public Mono<UserRest> getUserById(final UUID id) {
        log.info("📥 - UserServiceImpl.getUserById called with id: {}", id);

        return userRepository.findById(id)
                .mapNotNull(this::convertToRest);
    }

    @Override
    public Flux<UserRest> findAll(int page, final int size) {
        log.info("📥 - UserServiceImpl.findAll called with page: {}, size: {}", page, size);

        if (page > 0) {
            page = page - 1;
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllBy(pageable)
                .mapNotNull(this::convertToRest);

    }

    @Override
    public Flux<UserRest> streamUser() {
        return usersSink.asFlux()
                .doOnSubscribe(subscription -> log.info("📥 - UserServiceImpl.streamUser subscribed"))
                .doOnNext(userRest -> log.info("📤 - UserServiceImpl.streamUser emitted: {}", userRest))
                .doOnCancel(() -> log.info("📤 - UserServiceImpl.streamUser subscription cancelled"))
                .publish()
                .autoConnect(1);
    }

    private Mono<UserEntity> convertToEntity(final CreateUserRequest createUserRequest) {
        // Password encoding can be CPU intensive, so we offload it to a separate thread using boundedElastic scheduler for non-blocking behavior.
        return Mono.fromCallable(() -> {
            UserEntity userEntity = new UserEntity();
            BeanUtils.copyProperties(createUserRequest, userEntity);
            userEntity.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            return userEntity;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private UserRest convertToRest(final UserEntity userEntity) {
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest);
        return userRest;
    }

    @Override
    public Mono<UserDetails> findByUsername(final String username) {
        return userRepository.findByEmail(username)
                .map(userEntity ->
                        // Here you would convert your UserEntity to a UserDetails implementation
                        User.withUsername(userEntity.getEmail())
                                .password(userEntity.getPassword())
                                .authorities(new ArrayList<>())
                                .build());
    }
}
