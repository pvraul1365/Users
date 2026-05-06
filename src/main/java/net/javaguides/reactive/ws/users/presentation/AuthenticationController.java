package net.javaguides.reactive.ws.users.presentation;

import lombok.RequiredArgsConstructor;
import net.javaguides.reactive.ws.users.presentation.model.AutenticationRequest;
import net.javaguides.reactive.ws.users.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * AuthenticationController
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 06/05/2026 - 09:54
 * @since 1.17
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(@RequestBody Mono<AutenticationRequest> authenticationRequestMono) {
        return authenticationRequestMono
                .flatMap(autenticationRequest -> authenticationService.authenticate(autenticationRequest.getEmail(), autenticationRequest.getPassword()))
                .map(authenticationResultMap -> ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationResultMap.get("token"))
                        .header("UserId", authenticationResultMap.get("userId"))
                        .build());
    }

}
