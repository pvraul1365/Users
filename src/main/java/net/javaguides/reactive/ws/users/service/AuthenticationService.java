package net.javaguides.reactive.ws.users.service;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<Map<String, String>> authenticate(String username, String password);

}
