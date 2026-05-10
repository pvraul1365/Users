package net.javaguides.reactive.ws.users.infrastructure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * TestSecurityConfig
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 10/05/2026 - 11:41
 * @since 1.17
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain testSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

}
