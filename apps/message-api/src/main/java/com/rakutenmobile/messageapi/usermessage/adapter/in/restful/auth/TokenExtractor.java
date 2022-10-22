package com.rakutenmobile.messageapi.usermessage.adapter.in.restful.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenExtractor implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authorization = exchange.getRequest()
                .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        assert authorization != null;
        return Mono.just(new TokenHolder(authorization));
    }
}
