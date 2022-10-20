package com.rakutenmobile.userapi.user.application.port.out;

import com.rakutenmobile.userapi.user.domain.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetUserUseCase {
    Mono<User> getUserById(UUID userId);
}
