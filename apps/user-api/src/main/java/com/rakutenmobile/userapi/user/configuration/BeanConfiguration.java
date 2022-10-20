package com.rakutenmobile.userapi.user.configuration;

import com.rakutenmobile.userapi.UserApiApplication;
import com.rakutenmobile.userapi.user.adapter.out.persistence.UserRepository;
import com.rakutenmobile.userapi.user.application.port.in.AddUserUseCase;
import com.rakutenmobile.userapi.user.application.service.AddUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ComponentScan(basePackageClasses = UserApiApplication.class)
public class BeanConfiguration {

    @Bean
    AddUserUseCase customerService(final UserRepository userRepository) {
        return new AddUserService(userRepository);
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}
