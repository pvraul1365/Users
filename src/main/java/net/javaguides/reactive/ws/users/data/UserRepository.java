package net.javaguides.reactive.ws.users.data;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, UUID> {
}
