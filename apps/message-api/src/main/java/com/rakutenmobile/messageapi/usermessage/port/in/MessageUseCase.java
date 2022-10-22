package com.rakutenmobile.messageapi.usermessage.port.in;

import com.rakutenmobile.messageapi.usermessage.domain.UserMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface MessageUseCase {
    Mono<UserMessage> submitMessage(UserMessage message);
    Mono<UserMessage> getMessageById(java.util.UUID id);
    Mono<Void> deleteMessageById(java.util.UUID id);
    Mono<Page<UserMessage>> findAll(PageRequest pageRequest, Optional<String> userId, Optional<String> topic);
}
