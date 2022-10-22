package com.rakutenmobile.messageapi.usermessage.adapter.in.restful.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

@Configuration
public class Config {
    private UserApi userApi;
    private TokenExtractor tokenExtractor;
    @Autowired
    public Config(UserApi userApi, TokenExtractor tokenExtractor) {
        this.userApi = userApi;
        this.tokenExtractor = tokenExtractor;
    }

    @Bean
    AuthenticationWebFilter customAuthFilter(){
        AuthenticationWebFilter filter = new AuthenticationWebFilter(this.userApi);

        filter.setServerAuthenticationConverter(this.tokenExtractor);

        filter.setAuthenticationSuccessHandler(new WebFilterChainServerAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler((exchange, exception) ->
                Mono.error(new BadCredentialsException("Wrong authentication token")));

        return filter;
    }
}
