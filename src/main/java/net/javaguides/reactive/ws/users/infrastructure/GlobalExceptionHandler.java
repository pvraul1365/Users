package net.javaguides.reactive.ws.users.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

/**
 * GlobalExceptionHandler
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 05/05/2026 - 10:18
 * @since 1.17
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public Mono<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException exception) {
        log.error("❌ - DuplicateKeyException occurred: {}", exception.getMessage());
        return Mono.just(ErrorResponse.builder(
                        exception,
                        HttpStatus.CONFLICT,
                        "User with the same email already exists"
                )
                .build());

    }

    @ExceptionHandler(Exception.class)
    public Mono<ErrorResponse> handleGeneralException(Exception exception) {
        log.error("❌ - An unexpected error occurred: {}", exception.getMessage());
        return Mono.just(ErrorResponse.builder(
                        exception,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        exception.getMessage()
                )
                .build());

    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ErrorResponse> handleWebExchangeBindException(WebExchangeBindException exception) {
        log.error("❌ - WebExchangeBindException occurred: {}", exception.getMessage());
        final String errorMessage = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed for the request");

        return Mono.just(ErrorResponse.builder(
                        exception,
                        HttpStatus.BAD_REQUEST,
                        errorMessage
                )
                .build());

    }

}
