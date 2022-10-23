package com.rakutenmobile.messageapi.usermessage.configuration;

import com.rakutenmobile.messageapi.MessageApiApplication;
import com.rakutenmobile.messageapi.usermessage.adapter.in.restful.exception.DefaultGlobalExceptionHandler;
import com.rakutenmobile.messageapi.usermessage.adapter.out.kafka.Publisher;
import com.rakutenmobile.messageapi.usermessage.adapter.out.persistence.MessageRepository;
import com.rakutenmobile.messageapi.usermessage.domain.exception.MessageNotFoundException;
import com.rakutenmobile.messageapi.usermessage.domain.exception.MessageNotOwnedException;
import com.rakutenmobile.messageapi.usermessage.port.in.MessageUseCase;
import com.rakutenmobile.messageapi.usermessage.port.out.PublishMessageUseCase;
import com.rakutenmobile.messageapi.usermessage.port.service.MessageService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackageClasses = MessageApiApplication.class)
public class BeanConfiguration {

    private static final Logger log = LoggerFactory.getLogger(Publisher.class.getName());

    private static final String TOPIC = "send-message";
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";
    private static final String CLIENT_ID_CONFIG = "kafka-message-publisher";

    @Bean
    MessageUseCase messageUseCase(final MessageRepository messageRepository) {
        return new MessageService(messageRepository);
    }

    @Bean
    PublishMessageUseCase publishMessageUseCase(ReactiveKafkaProducerTemplate kafka) {
        return new Publisher(kafka);
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new ReactiveKafkaProducerTemplate<String, String>(SenderOptions.create(props));
    }

    @Bean
    public HttpStatus defaultStatus() {
        return HttpStatus.UNAUTHORIZED;
    }

    @Bean
    public Map<Class<? extends Exception>, HttpStatus> exceptionToStatusCode() {
        return Map.of(
                MessageNotFoundException.class, HttpStatus.NOT_FOUND,
                MessageNotOwnedException.class, HttpStatus.FORBIDDEN,
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
