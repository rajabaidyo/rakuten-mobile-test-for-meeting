package com.rakutenmobile.messageapi.usermessage.adapter.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rakutenmobile.messageapi.usermessage.adapter.out.kafka.Publisher;
import com.rakutenmobile.messageapi.usermessage.adapter.out.kafka.UserMessageDto;
import com.rakutenmobile.messageapi.usermessage.domain.UserMessage;
import com.rakutenmobile.messageapi.usermessage.port.in.MessageUseCase;
import com.rakutenmobile.messageapi.usermessage.port.out.ConsumeMessageUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;

@Service
@PropertySource("classpath:application.properties")
public class Consumer implements ConsumeMessageUseCase, CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Publisher.class.getName());

    private final String kafkaTopic;

    private final ReceiverOptions<String, String> receiverOptions;

    private final MessageUseCase messageUseCase;

    public Consumer(@Qualifier("kafka.topic.send-message") String kafkaTopic, ReceiverOptions<String, String> receiverOptions, MessageUseCase messageUseCase) {
        this.kafkaTopic = kafkaTopic;
        this.receiverOptions = receiverOptions;
        this.messageUseCase = messageUseCase;
    }
    @Override
    public Flux<ReceiverRecord<String, String>> consume() {
        ReceiverOptions<String, String> options = receiverOptions.subscription(Collections.singleton(kafkaTopic))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        return KafkaReceiver.create(options).receive();
        
    }

    @Override
    public void run(String... args) throws Exception {
        consume().subscribe(record -> {
            ReceiverOffset offset = record.receiverOffset();
            System.out.println(record.toString());
            record.value();
            ObjectMapper mapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            try {
                UserMessageDto dto = mapper.readValue(record.value(), UserMessageDto.class);
                // insert to db
                UserMessage userMessage = UserMessage.builder().userId(dto.getUserId())
                        .createdAt(dto.getCreatedAt())
                        .topic(dto.getTopic())
                        .content(dto.getContent())
                        .build();
                messageUseCase.submitMessage(userMessage).subscribe(v -> {
                    System.out.println("Success insert "+v.toString());
                    offset.acknowledge();
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
