package com.rakutenmobile.userapi.user.adapter.in.restful.auth;

import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import com.rakutenmobile.userapi.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
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
            List<GrantedAuthority> authorities = new ArrayList(1);
            authorities.add(new SimpleGrantedAuthority( "USER"));
            CustomUserDetails customUserDetails = new CustomUserDetails(v.getName(),
                    v.getUserId().toString(), v.getPassword(), v.getCreatedAt(), authorities);
            return customUserDetails;
        });
    }
}
