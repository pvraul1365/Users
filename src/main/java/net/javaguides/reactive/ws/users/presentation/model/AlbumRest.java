package net.javaguides.reactive.ws.users.presentation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AlbumRest
 * <p>
 * Created by IntelliJ, Spring Framework Guru.
 *
 * @author architecture - pvraul
 * @version 09/05/2026 - 20:29
 * @since 1.17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlbumRest {

    private String id;
    private String title;

}
