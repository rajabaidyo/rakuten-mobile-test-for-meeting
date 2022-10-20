package com.rakutenmobile.userapi.user.application.port.out;

import com.rakutenmobile.userapi.user.domain.User;

import java.util.Optional;

public interface GetUserUseCase {
    Optional<User> GetUserByIdAndPassword(java.util.UUID userId, String password);
}
