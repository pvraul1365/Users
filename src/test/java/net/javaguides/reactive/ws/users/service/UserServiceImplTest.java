package net.javaguides.reactive.ws.users.service;

import java.util.UUID;
import java.util.function.Function;
import net.bytebuddy.build.Plugin;
import net.javaguides.reactive.ws.users.data.UserEntity;
import net.javaguides.reactive.ws.users.data.UserRepository;
import net.javaguides.reactive.ws.users.presentation.model.AlbumRest;
import net.javaguides.reactive.ws.users.presentation.model.CreateUserRequest;
import net.javaguides.reactive.ws.users.presentation.model.UserRest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    WebClient webClient;

    private Sinks.Many<UserRest> usersSink;
    private UserServiceImpl  userService;

    @BeforeEach
    void setUp() {
        usersSink = Sinks.many().multicast().onBackpressureBuffer();
        userService = new UserServiceImpl(userRepository, passwordEncoder, usersSink, webClient);
    }

    @Test
    void testCreateUser_withValidRequest_returnCreatedUserDetails() {

        // Arrange
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .password("123456789")
                .build();

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setFirstName(createUserRequest.getFirstName());
        savedEntity.setLastName(createUserRequest.getLastName());
        savedEntity.setEmail(createUserRequest.getEmail());
        savedEntity.setPassword(createUserRequest.getPassword());

        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(Mono.just(savedEntity));

        // Act
        Mono<UserRest> result = userService.createUser(Mono.just(createUserRequest));

        // Assert
        StepVerifier.create(result)
                .assertNext(userRest -> {
                    assertNotNull(userRest);
                    assertEquals(savedEntity.getId(), userRest.getId());
                    assertEquals(savedEntity.getFirstName(), userRest.getFirstName());
                    assertEquals(savedEntity.getLastName(), userRest.getLastName());
                    assertEquals(savedEntity.getEmail(), userRest.getEmail());
                })
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserEntity.class));
    }

    @Test
    void testCreateUser_withValidRequest_EmitsEventToSink() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .password("123456789")
                .build();

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setFirstName(request.getFirstName());
        savedEntity.setLastName(request.getLastName());
        savedEntity.setEmail(request.getEmail());
        savedEntity.setPassword(request.getPassword());

        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(Mono.just(savedEntity));

        // Subscribe to the sink before triggering the service call.
        Flux<UserRest> sinkFlux = usersSink.asFlux();

        // Act and Assert
        StepVerifier.create(userService.createUser(Mono.just(request))
                        .thenMany(usersSink.asFlux().take(1)))
                .expectNextMatches(userRest -> userRest.getId().equals(savedEntity.getId()) &&
                        userRest.getFirstName().equals(savedEntity.getFirstName()) &&
                        userRest.getLastName().equals(savedEntity.getLastName()) &&
                        userRest.getEmail().equals(savedEntity.getEmail()))
                .verifyComplete();
    }

    @Test
    void testGetUserById_WithExistingUser_ReturnsUserRest() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        userEntity.setEmail("test@test.com");

        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(userEntity));

        // Act
        Mono<UserRest> result = userService.getUserById(userId, null, "jwt-token");

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(userRest -> userRest.getId().equals(userId) &&
                        userRest.getFirstName().equals(userEntity.getFirstName()) &&
                        userRest.getLastName().equals(userEntity.getLastName()) &&
                        userRest.getEmail().equals(userEntity.getEmail()) &&
                        userRest.getAlbums() == null)
                .verifyComplete();

        // Verify that findById() method was called once
        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);

        // Verify that WebClient is not invoked when include is null
        Mockito.verify(webClient, Mockito.never()).get();
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void testGetUserById_WithIncludeAlbums_ReturnsAlbums() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String jwt = "valid-jwt";

        // 1. Setup UserEntity
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        userEntity.setEmail("test@test.com");
        userEntity.setPassword("encodedPass");

        // 2. Mock repository response
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(userEntity));

        // 3. Mock WebClient response with albums
        WebClient.RequestHeadersUriSpec getSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        Mockito.when(webClient.get()).thenReturn(getSpec);
        Mockito.when(getSpec.uri(Mockito.any(Function.class))).thenReturn(headersSpec);
        Mockito.when(headersSpec.header(Mockito.eq("Authorization"), Mockito.eq(jwt))).thenReturn(headersSpec);
        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);

        // Explicitly return test albums
        AlbumRest album1 = new AlbumRest("album1", "Summer Vacation");
        AlbumRest album2 = new AlbumRest("album2", "Family Reunion");
        Mockito.when(responseSpec.bodyToFlux(AlbumRest.class)).thenReturn(Flux.just(album1, album2));

        // Act
        Mono<UserRest> result = userService.getUserById(userId, "albums", jwt);

        // Assert: Verify albums are present
        StepVerifier.create(result)
                .expectNextMatches(user -> {
                    // Verify user details
                    assertEquals(userId, user.getId(), "User ID mismatch");
                    assertEquals(userEntity.getFirstName(), user.getFirstName(), "First name mismatch");
                    assertEquals(userEntity.getLastName(), user.getLastName(), "Last name mismatch");
                    assertEquals(userEntity.getEmail(), user.getEmail(), "Email mismatch");

                    // Verify albums
                    assertNotNull(user.getAlbums(), "Albums list should not be null");
                    assertEquals(2, user.getAlbums().size(), "Incorrect number of albums");
                    assertEquals("Summer Vacation", user.getAlbums().get(0).getTitle());
                    assertEquals("Family Reunion", user.getAlbums().get(1).getTitle());
                    return true;
                })
                .verifyComplete();

        // Verify repository call
        Mockito.verify(userRepository).findById(userId);
    }

}