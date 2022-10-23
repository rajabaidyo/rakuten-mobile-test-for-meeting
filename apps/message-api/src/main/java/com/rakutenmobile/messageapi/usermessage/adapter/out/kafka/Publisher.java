package com.rakutenmobile.messageapi.usermessage.adapter.out.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rakutenmobile.messageapi.usermessage.domain.UserMessage;
import com.rakutenmobile.messageapi.usermessage.port.out.PublishMessageUseCase;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.*;
import java.util.Set;
import java.util.stream.Collectors;

public class Publisher implements PublishMessageUseCase {
    private String kafkaTopic;

    private final ReactiveKafkaProducerTemplate<String, String> kafka;

    private final Validator validator;

    public Publisher(ReactiveKafkaProducerTemplate<String, String> kafka, String kafkaSendTopic) {
        this.kafka = kafka;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
        this.kafkaTopic = kafkaSendTopic;
    }

    @Override
    public Mono<Void> publish(Flux<UserMessage> messages) {
        return messages
                .map(message -> {
                    UserMessageDto dto = new UserMessageDto(
                            message.getContent(), message.getTopic(), message.getCreatedAt(), message.getUserId());
                    return dto;
                })
                .map(message -> {
            UserMessageDto dto = new UserMessageDto(
                    message.getContent(), message.getTopic(), message.getCreatedAt(), message.getUserId());
            ObjectMapper mapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            String json = "";
            try {
                json = mapper.writeValueAsString(dto);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return kafka.send(kafkaTopic, json).subscribe();
        }).then();
    }
}
