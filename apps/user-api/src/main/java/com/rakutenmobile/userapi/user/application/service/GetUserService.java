package com.rakutenmobile.userapi.user.application.service;

import com.rakutenmobile.userapi.user.adapter.out.persistence.UserRepository;
import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import com.rakutenmobile.userapi.user.domain.User;
import com.rakutenmobile.userapi.user.domain.exception.UserNotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class GetUserService implements GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new UserNotFoundException("User Not Found"))))
                .map(x -> {
                   final User user = User.builder()
                           .userId(x.getUserId())
                           .name(x.getName())
                           .password(x.getPassword())
                           .createdAt(x.getCreatedAt())
                           .build();
                   return user;
                });
    }
}
