package com.rakutenmobile.userapi.user.application.service;

import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import com.rakutenmobile.userapi.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public class GetUserService implements GetUserUseCase {
    @Override
    public Optional<User> GetUserByIdAndPassword(UUID userId, String password) {
        return Optional.empty();
    }
}
