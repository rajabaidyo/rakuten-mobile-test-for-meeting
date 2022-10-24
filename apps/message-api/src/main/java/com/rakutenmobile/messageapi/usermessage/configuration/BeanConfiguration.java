package com.rakutenmobile.messageapi.usermessage.configuration;

import com.rakutenmobile.messageapi.MessageApiApplication;
import com.rakutenmobile.messageapi.lib.profanity.DefaultImpl;
import com.rakutenmobile.messageapi.usermessage.adapter.in.restful.exception.DefaultGlobalExceptionHandler;
import com.rakutenmobile.messageapi.usermessage.adapter.out.kafka.Publisher;
import com.rakutenmobile.messageapi.usermessage.adapter.out.persistence.MessageRepository;
import com.rakutenmobile.messageapi.usermessage.domain.exception.MessageNotFoundException;
import com.rakutenmobile.messageapi.usermessage.domain.exception.MessageNotOwnedException;
import com.rakutenmobile.messageapi.usermessage.port.in.MessageUseCase;
import com.rakutenmobile.messageapi.usermessage.port.out.PublishMessageUseCase;
import com.rakutenmobile.messageapi.usermessage.port.service.MessageService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackageClasses = MessageApiApplication.class)
@PropertySource("classpath:application.properties")
public class BeanConfiguration {
    private static final String CLIENT_ID_CONFIG = "kafka-message-publisher";

    @Resource
    public Environment env;

    @Bean
    MessageUseCase messageUseCase(final MessageRepository messageRepository) {
        return new MessageService(messageRepository);
    }

    @Bean
    PublishMessageUseCase publishMessageUseCase(ReactiveKafkaProducerTemplate kafka) {
        return new Publisher(kafka, kafkaSendMessageTopic());
    }

    @Bean
    @Qualifier("kafka.topic.send-message")
    String kafkaSendMessageTopic() {
        return env.getRequiredProperty("app.kafka.topic.send-message");
    }

    @Bean
    @Qualifier("kafka.topic.send-message.deadletter")
    String kafkaDeadLetterTopic() {
        return env.getRequiredProperty("app.kafka.topic.send-message.deadletter");
    }
    @Bean
    @Qualifier("auth.server.base-url")
    String authServerBaseUrl() {
        return env.getRequiredProperty("app.auth.server.base-url");
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getRequiredProperty("spring.kafka.producer.bootstrap-servers"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new ReactiveKafkaProducerTemplate<String, String>(SenderOptions.create(props));
    }

    @Bean
    public ReceiverOptions<String, String> receiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getRequiredProperty("spring.kafka.consumer.bootstrap-servers"));
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return ReceiverOptions.create(props);
    }

    @Bean
    public HttpStatus defaultStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
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

    private KafkaOperations<String, Object> getEventKafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getRequiredProperty("spring.kafka.producer.bootstrap-servers"));
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer() {
        return new DeadLetterPublishingRecoverer(getEventKafkaTemplate(),
                (record, ex) -> new TopicPartition(kafkaDeadLetterTopic(), 1));
    }

}
