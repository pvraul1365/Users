package net.javaguides.reactive.ws.users.data;

import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setUp() {

        UserEntity userEntity1 = UserEntity.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.net")
                .password("123456789")
                .build();

        UserEntity userEntity2 = UserEntity.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.net")
                .password("123456789")
                .build();

        final String inserSql = "INSERT INTO users (id, first_name, last_name, email, password) VALUES (:id, :firstName, :lastName, :email, :password)";

        Flux.just(userEntity1, userEntity2)
                .concatMap(userEntity -> databaseClient.sql(inserSql)
                        .bind("id", userEntity.getId())
                        .bind("firstName", userEntity.getFirstName())
                        .bind("lastName", userEntity.getLastName())
                        .bind("email", userEntity.getEmail())
                        .bind("password", userEntity.getPassword())
                        .fetch()
                        .rowsUpdated())
                        .then()
                        .as(StepVerifier::create)
                        .verifyComplete();
    }

    @AfterAll
    void tearDown() {

        databaseClient.sql("TRUNCATE TABLE users")
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

    }

    @Test
    void testFindByEmail_WithEmailThatExists_ReturnsMatchingUser() {

        // Arrange
        final String emailToFind = "john.doe@example.net";

        // Act and Assert
        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextMatches(userEntity -> emailToFind.equals(userEntity.getEmail()))
                .verifyComplete();

    }

    @Test
    void testFindAllBy_WithValidPageable_ReturnsPaginatedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2); // First page, page size = 2

        // Act & Assert
        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(2) // Expect exactly 2 items on the first page
                .verifyComplete();
    }

    @Test
    void testFindAllBy_WithNonExistentPage_ReturnsEmptyFlux() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 3); // Second page, page size = 2 (no data exists here)

        // Act & Assert
        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(0) // Expect no items on the second page
                .expectComplete()
                .verify();
    }

    @Test
    void testSave_whenExistingEmailProvided_shouldFail() {
        UserEntity invalidUser = new UserEntity(null, "Sergey", "Kargopolov", "jane.doe@example.net", "password");

        userRepository.save(invalidUser)
                .as(StepVerifier::create)
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    @Test
    void testSave_whenValidUserProvided_shouldSucceed() {
        UserEntity validUser = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@example.net")
                .password("123456789")
                .build();

        userRepository.save(validUser)
                .as(StepVerifier::create)
                .expectNextMatches(savedUser -> {
                    return savedUser.getId() != null
                            && savedUser.getFirstName().equals(validUser.getFirstName())
                            && savedUser.getLastName().equals(validUser.getLastName())
                            && savedUser.getEmail().equals(validUser.getEmail());
                })
                .verifyComplete();
    }

}