package com.rakutenmobile.messageapi.usermessage.adapter.in.kafka;

import reactor.kafka.receiver.ReceiverRecord;

public class ReceiverRecordException extends RuntimeException {
    private final ReceiverRecord record;

    ReceiverRecordException(ReceiverRecord record, Throwable t) {
        super(t);
        this.record = record;
    }

    public ReceiverRecord getRecord() {
        return this.record;
    }
}
