package com.rakutenmobile.messageapi.usermessage.adapter.in.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rakutenmobile.messageapi.lib.profanity.Filter;
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
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.lang.RuntimeException;
@Service
@PropertySource("classpath:application.properties")
public class Consumer implements ConsumeMessageUseCase, CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Publisher.class.getName());

    private final String kafkaTopic;

    private final ReceiverOptions<String, String> receiverOptions;

    private final MessageUseCase messageUseCase;

    private final DeadLetterPublishingRecoverer deadLetterPublishingRecoverer;

    private final Filter profanityFilter;

    public Consumer(@Qualifier("kafka.topic.send-message") String kafkaTopic, ReceiverOptions<String, String> receiverOptions,
                    MessageUseCase messageUseCase, DeadLetterPublishingRecoverer deadLetterPublishingRecoverer, Filter profanityFilter) {
        this.kafkaTopic = kafkaTopic;
        this.receiverOptions = receiverOptions;
        this.messageUseCase = messageUseCase;
        this.deadLetterPublishingRecoverer = deadLetterPublishingRecoverer;
        this.profanityFilter = profanityFilter;
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
        consume()
                .doOnNext(record -> {
                    ReceiverOffset offset = record.receiverOffset();
                    System.out.println(record.toString());
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
                        if (profanityFilter.check(userMessage.getContent())) {
                            throw new ReceiverRecordException(record, new RuntimeException("message contain forbidden words"));
                        }
                        messageUseCase.submitMessage(userMessage).subscribe(v -> {
                            System.out.println("Success insert " + v.toString());
                            offset.acknowledge();
                        });
                    } catch (ReceiverRecordException e) {
                        deadLetterPublishingRecoverer.accept(record, e);
                    }
                    catch (Exception e) {
                        throw new ReceiverRecordException(record, new RuntimeException(e.getMessage()));
                    }
                })
                .doOnError(System.out::println)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).transientErrors(true))
                .onErrorContinue((e, record) -> {
                    ReceiverRecordException ex = (ReceiverRecordException) e;
                    System.out.println("Retries exhausted for " + ex);
                    deadLetterPublishingRecoverer.accept(ex.getRecord(), ex);
                    ex.getRecord().receiverOffset().acknowledge();
                })
                .repeat()
                .subscribe();
    }
}
