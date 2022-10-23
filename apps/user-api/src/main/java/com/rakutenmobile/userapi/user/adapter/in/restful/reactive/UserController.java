package com.rakutenmobile.userapi.user.adapter.in.restful.reactive;

import com.rakutenmobile.openapi.models.RegisterRequest;
import com.rakutenmobile.openapi.models.User;
import com.rakutenmobile.openapi.spring.reactive.api.RegisterApi;
import com.rakutenmobile.openapi.spring.reactive.api.ValidateApi;
import com.rakutenmobile.userapi.user.adapter.in.restful.auth.CustomUserDetails;
import com.rakutenmobile.userapi.user.application.port.in.AddUserUseCase;
import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@RestController
public class UserController implements RegisterApi, ValidateApi {

    private final AddUserUseCase addUserUseCase;
    private final GetUserUseCase getUserUseCase;

    public UserController(AddUserUseCase addUserUseCase, GetUserUseCase getUserUseCase) {
        this.addUserUseCase = addUserUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @Override
    public Mono<ResponseEntity<User>> registerPost(RegisterRequest req, ServerWebExchange exchange) {
        Mono<com.rakutenmobile.userapi.user.domain.User> result = addUserUseCase.AddSingleUser(req.getName(), req.getPassword());
        return result.map(resp -> {
            final User userDto = new User();
            userDto.setUserId(resp.getUserId().toString());
            userDto.setName(resp.getName());
            return new ResponseEntity<>(userDto, HttpStatus.CREATED);
        });
    }

    @Override
    public Mono<ResponseEntity<User>> validateGet(ServerWebExchange exchange) {
        Mono<CustomUserDetails> userDetail = ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getPrincipal()).cast(CustomUserDetails.class);
        return userDetail.map(resp -> {
            final User userDto = new User();
            userDto.setUserId(resp.getUsername());
            userDto.setName(resp.getName());
            userDto.setCreatedAt(resp.getCreatedAt());
            userDto.setRole(resp.stringAuthorities());
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        });
    }
}
