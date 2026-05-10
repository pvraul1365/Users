package net.javaguides.reactive.ws.users.infrastructure;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientConfig
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 09/05/2026 - 20:01
 * @since 1.17
 */
@Configuration
public class WebClientConfig {

    @Value("${external.api.base-uri:http://localhost}")
    private String baseUri;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(this.baseUri)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .build();
    }

}
