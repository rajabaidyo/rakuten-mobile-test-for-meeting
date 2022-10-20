package com.rakutenmobile.userapi.user.adapter.in.restful.reactive;

import com.rakutenmobile.openapi.models.RegisterRequest;
import com.rakutenmobile.openapi.models.User;
import com.rakutenmobile.openapi.spring.reactive.api.RegisterApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class UserController implements RegisterApi {
    @Override
    public Mono<ResponseEntity<User>> registerPost(Mono<RegisterRequest> registerRequest, ServerWebExchange exchange) {
        return null;
    }
}
