package com.rakutenmobile.userapi.user.adapter.in.restful.reactive;

import com.rakutenmobile.openapi.models.RegisterRequest;
import com.rakutenmobile.openapi.models.User;
import com.rakutenmobile.openapi.spring.reactive.api.RegisterApi;
import com.rakutenmobile.userapi.user.application.port.in.AddUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@RestController
public class UserController implements RegisterApi {

    private final AddUserUseCase addUserUseCase;

    public UserController(AddUserUseCase addUserUseCase) {
        this.addUserUseCase = addUserUseCase;
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
}
