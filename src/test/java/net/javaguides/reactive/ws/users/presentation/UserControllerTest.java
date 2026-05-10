package net.javaguides.reactive.ws.users.presentation;

import java.util.UUID;
import net.javaguides.reactive.ws.users.infrastructure.TestSecurityConfig;
import net.javaguides.reactive.ws.users.presentation.model.CreateUserRequest;
import net.javaguides.reactive.ws.users.presentation.model.UserRest;
import net.javaguides.reactive.ws.users.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

@WebFluxTest(controllers = UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testCreateUser_withValidRequest_returnCreatedStatusAndUserDetails() {

        // Arrange
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("123456789")
                .build();

        UUID uerId = UUID.randomUUID();
        String expectedLocation = "/api/v1/users/" + uerId;

        UserRest expectedUserRest = UserRest.builder()
                .id(uerId)
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .email(createUserRequest.getEmail())
                .albums(null)
                .build();

        Mockito.when(userService.createUser(Mockito.<Mono<CreateUserRequest>>any())).thenReturn(Mono.just(expectedUserRest));

        // Act
        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(expectedLocation)
                .expectBody(UserRest.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(expectedUserRest.getId(), response.getId());
                    assertEquals(expectedUserRest.getFirstName(), response.getFirstName());
                    assertEquals(expectedUserRest.getLastName(), response.getLastName());
                    assertEquals(expectedUserRest.getEmail(), response.getEmail());
                });

        // Assert
        Mockito.verify(userService, Mockito.times(1)).createUser(Mockito.<Mono<CreateUserRequest>>any());

    }

    @Test
    void testCreateUser_withInvalidRequest_returnsBadRequest() {
        // Arrange
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("123")
                .build();

        // Act & Assert
        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verify(userService, Mockito.never()).createUser(Mockito.any());
    }

    @Test
    void testCreateUser_withEmptyFirstName_returnsBadRequest() {
        // Arrange
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("") // Empty first name
                .lastName("Doe")
                .email("john@example.com")
                .password("123")
                .build();

        // Act & Assert
        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verify(userService, Mockito.never()).createUser(Mockito.any());
    }

    @Test
    void testCreateUser_whenServiceThrowsException_returnsInternalServerErrorWithExpectedStructure() {
        // Arrange
        CreateUserRequest validRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("123456789")
                .build();

        Mockito.when(userService.createUser(Mockito.any())).thenReturn(Mono.error(new RuntimeException("Service error")));

        // Act & Assert
        webTestClient
                .post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                .expectBody()
                .jsonPath("$.instance").isEqualTo("/api/v1/users")
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.detail").isEqualTo("Service error");

        Mockito.verify(userService, Mockito.times(1)).createUser(Mockito.any());
    }
}