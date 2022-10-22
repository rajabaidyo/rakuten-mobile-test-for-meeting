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

public class Publisher implements PublishMessageUseCase {
    private static final String TOPIC = "send-message";

    private final ReactiveKafkaProducerTemplate<String, String> kafka;

    public Publisher(ReactiveKafkaProducerTemplate<String, String> kafka) {
        this.kafka = kafka;
    }

    @Override
    public Mono<Void> publish(Flux<UserMessage> messages) {
        return messages.map(message -> {
            UserMessageDto dto = new UserMessageDto(message.getId(),
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
            return kafka.send(TOPIC, json).subscribe();
        }).then();
    }
}
