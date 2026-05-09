package net.javaguides.reactive.ws.users.infrastructure;

import net.javaguides.reactive.ws.users.presentation.model.UserRest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * SinksConfig
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 09/05/2026 - 16:26
 * @since 1.17
 */
@Configuration
public class SinksConfig {

    @Bean
    public Sinks.Many<UserRest> usersSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

}
