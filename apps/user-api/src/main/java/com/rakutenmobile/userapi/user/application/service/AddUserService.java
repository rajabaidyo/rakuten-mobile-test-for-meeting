package com.rakutenmobile.userapi.user.application.service;

import com.rakutenmobile.userapi.user.adapter.out.persistence.UserEntity;
import com.rakutenmobile.userapi.user.adapter.out.persistence.UserRepository;
import com.rakutenmobile.userapi.user.application.port.in.AddUserUseCase;
import com.rakutenmobile.userapi.user.domain.User;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class AddUserService implements AddUserUseCase {

    private final UserRepository userRepository;

    public AddUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> AddSingleUser(String name, String password) {
        UserEntity newUser = UserEntity.builder().name(name).password(password).build();
        Mono<UserEntity> saved = userRepository.save(newUser);
        return saved.map(userEntity -> User.builder().userId(userEntity.getUserId())
                .name(userEntity.getName()).password(userEntity.getPassword())
                .createdAt(userEntity.getCreatedAt())
                .build());
    }
}
