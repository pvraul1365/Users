package net.javaguides.reactive.ws.users.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.reactive.ws.users.data.UserEntity;
import net.javaguides.reactive.ws.users.data.UserRepository;
import net.javaguides.reactive.ws.users.presentation.CreateUserRequest;
import net.javaguides.reactive.ws.users.presentation.UserRest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        log.info("📥 - UserServiceImpl.createUser called with data: {}", createUserRequestMono);

        return createUserRequestMono
                .mapNotNull(this::convertToEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToRest);
    }

    @Override
    public Mono<UserRest> getUserById(final UUID id) {
        log.info("📥 - UserServiceImpl.getUserById called with id: {}", id);

        return userRepository.findById(id)
                .mapNotNull(this::convertToRest);
    }

    private UserEntity convertToEntity(final CreateUserRequest createUserRequest){
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(createUserRequest,userEntity);
        return userEntity;
    }

    private UserRest convertToRest(final UserEntity userEntity){
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity,userRest);
        return userRest;
    }
}
