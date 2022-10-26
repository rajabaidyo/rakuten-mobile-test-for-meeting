package com.rakutenmobile.userapi.user.configuration;

import com.rakutenmobile.userapi.UserApiApplication;
import com.rakutenmobile.userapi.user.adapter.in.restful.reactive.exception.DefaultGlobalExceptionHandler;
import com.rakutenmobile.userapi.user.adapter.out.persistence.UserRepository;
import com.rakutenmobile.userapi.user.application.port.in.AddUserUseCase;
import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import com.rakutenmobile.userapi.user.application.service.AddUserService;
import com.rakutenmobile.userapi.user.application.service.GetUserService;
import com.rakutenmobile.userapi.user.domain.exception.UserNotFoundException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.Map;

@Configuration
@ComponentScan(basePackageClasses = UserApiApplication.class)
public class BeanConfiguration {

    @Bean
    AddUserUseCase addUserUseCase(final UserRepository userRepository) {
        return new AddUserService(userRepository);
    }

    @Bean
    GetUserUseCase getUserUseCase(final UserRepository userRepository) {
        return new GetUserService(userRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public HttpStatus defaultStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Bean
    public Map<Class<? extends Exception>, HttpStatus> exceptionToStatusCode() {
        return Map.of(
                UserNotFoundException.class, HttpStatus.UNAUTHORIZED,
                IllegalArgumentException.class, HttpStatus.BAD_REQUEST,
                DataIntegrityViolationException.class, HttpStatus.BAD_REQUEST,
                UnsupportedMediaTypeStatusException.class, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ServerWebInputException.class, HttpStatus.BAD_REQUEST,
                WebClientResponseException.Unauthorized.class, HttpStatus.UNAUTHORIZED,
                WebClientResponseException.Forbidden.class, HttpStatus.FORBIDDEN,
                WebExchangeBindException.class, HttpStatus.BAD_REQUEST
        );
    }

    @Bean
    @Order(-2)
    public DefaultGlobalExceptionHandler reactiveExceptionHandler(WebProperties webProperties, ApplicationContext applicationContext,
                                                                  ServerCodecConfigurer configurer) {
        DefaultGlobalExceptionHandler exceptionHandler = new DefaultGlobalExceptionHandler(
                new DefaultErrorAttributes(), webProperties.getResources(),
                applicationContext, exceptionToStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR
        );
        exceptionHandler.setMessageWriters(configurer.getWriters());
        exceptionHandler.setMessageReaders(configurer.getReaders());
        return exceptionHandler;
    }

}
