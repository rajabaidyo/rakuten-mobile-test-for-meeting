package com.rakutenmobile.userapi.user.adapter.in.restful.auth;

import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import com.rakutenmobile.userapi.user.domain.User;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class DbUserDetailsService implements ReactiveUserDetailsService {

    private final GetUserUseCase getUserUseCase;

    public DbUserDetailsService(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        Mono<User> user = getUserUseCase.getUserById(UUID.fromString(username));
        return user.map(v -> {
            org.springframework.security.core.userdetails.User.UserBuilder userBuilder = org.springframework.security.core.userdetails.User.builder();
            UserDetails userDetail = userBuilder.username(v.getUserId().toString())
                    .password(v.getPassword())
                    .roles("USER")
                    .build();
            return userDetail;
        });
    }
}