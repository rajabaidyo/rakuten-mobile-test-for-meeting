package com.rakutenmobile.userapi.user.application.port.in;

import com.rakutenmobile.userapi.user.domain.User;
import reactor.core.publisher.Mono;

public interface AddUserUseCase {
    Mono<User> AddSingleUser(String name, String password);
}
