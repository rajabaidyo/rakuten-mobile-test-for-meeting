package com.rakutenmobile.messageapi.usermessage.port.out;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverRecord;

public interface ConsumeMessageUseCase {
    Flux<ReceiverRecord<String, String>> consume();
}
