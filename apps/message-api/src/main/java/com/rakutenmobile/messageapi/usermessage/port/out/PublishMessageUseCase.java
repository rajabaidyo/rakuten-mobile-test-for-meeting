package com.rakutenmobile.messageapi.usermessage.port.out;

import com.rakutenmobile.messageapi.usermessage.domain.UserMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PublishMessageUseCase {
    Mono<Void> publish(Flux<UserMessage> messages);
}
