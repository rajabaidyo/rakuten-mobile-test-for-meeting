package com.rakutenmobile.messageapi.usermessage.adapter.in.restful.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class UserApi implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        // call to user-api
        String credentials = (String) authentication.getCredentials();
        WebClient webClient = WebClient.builder().baseUrl("http://localhost:8080/validate")
                .defaultHeader("Authorization", credentials).build();
        Mono<String> res = webClient.get().retrieve().bodyToMono(String.class);
        return res
                .map(v -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<String, String> resp = mapper.readValue(v, Map.class);
                User.UserBuilder userBuilder = User.builder();
                UserDetails userDetail = userBuilder.username(resp.get("userId"))
                        .password("")
                        .roles("USER")
                        .build();
                return new TokenHolder(userDetail, credentials, userDetail.getAuthorities());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
