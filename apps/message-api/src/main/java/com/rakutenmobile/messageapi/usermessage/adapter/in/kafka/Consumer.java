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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class Consumer implements ConsumeMessageUseCase, CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Publisher.class.getName());

    private static final String TOPIC = "send-message";
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";

    private final ReceiverOptions<String, String> receiverOptions;

    private final MessageUseCase messageUseCase;

    public Consumer(MessageUseCase messageUseCase) {
        this.messageUseCase = messageUseCase;
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        receiverOptions = ReceiverOptions.create(props);
    }
    @Override
    public Flux<ReceiverRecord<String, String>> consume() {
        ReceiverOptions<String, String> options = receiverOptions.subscription(Collections.singleton(TOPIC))
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
