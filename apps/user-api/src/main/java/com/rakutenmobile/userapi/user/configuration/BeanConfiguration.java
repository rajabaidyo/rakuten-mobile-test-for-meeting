package com.rakutenmobile.userapi.user.configuration;

import com.rakutenmobile.userapi.UserApiApplication;
import com.rakutenmobile.userapi.user.adapter.out.persistence.UserRepository;
import com.rakutenmobile.userapi.user.application.port.in.AddUserUseCase;
import com.rakutenmobile.userapi.user.application.port.out.GetUserUseCase;
import com.rakutenmobile.userapi.user.application.service.AddUserService;
import com.rakutenmobile.userapi.user.application.service.GetUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

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

}
