package net.javaguides.reactive.ws.users.infrastructure;

import java.sql.SQLException;
import org.h2.tools.Server;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

/**
 * H2ConsoleConfiguration
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 03/05/2026 - 20:18
 * @since 1.17
 */
@Configuration
//@Profile({"dev","test"})
@Profile("!prod & !production")
public class H2ConsoleConfiguration {

    private Server webServer;

    @EventListener(ApplicationStartedEvent.class)
    public void start() throws SQLException {
        String WEB_PORT="8082";
        this.webServer = Server.createWebServer("-webPort", WEB_PORT).start();
    }

    @EventListener(ContextClosedEvent.class)
    public void stop() {
        this.webServer.stop();
    }
}
